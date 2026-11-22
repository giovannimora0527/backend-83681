package com.uniminuto.clinica.modulos.despacho;

import com.lowagie.text.pdf.PdfPTable;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.modulos.inventario.InventarioService;
import com.uniminuto.clinica.security.CurrentUser;
import com.uniminuto.clinica.support.Db;
import com.uniminuto.clinica.support.In;
import com.uniminuto.clinica.support.PdfKit;
import com.uniminuto.clinica.util.PasswordUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

/**
 * Despacho de fórmulas (perfil DESPACHO_MEDICO). Único punto donde se descuenta inventario:
 * asignación por lote FEFO (primero en vencer, primero en salir), sin lotes vencidos, con
 * despacho parcial, confirmación transaccional y reversión solo con autorización de un administrador.
 */
@Service
public class DespachoService {

    private static final List<String> DESPACHABLES = List.of("PENDIENTE_DESPACHO", "EN_PREPARACION", "DESPACHO_PARCIAL",
            "PENDIENTE_DISPONIBILIDAD");

    private static final String LOTES_USABLES = """
            SELECT id, numero_lote, fecha_vencimiento, cantidad_disponible FROM medicamento_lote
             WHERE medicamento_id = ? AND activo = 1 AND cantidad_disponible > 0
               AND (fecha_vencimiento IS NULL OR fecha_vencimiento >= CURDATE())
             ORDER BY (fecha_vencimiento IS NULL), fecha_vencimiento, id
            """;

    private final Db db;
    private final CurrentUser current;
    private final InventarioService inventario;

    public DespachoService(Db db, CurrentUser current, InventarioService inventario) {
        this.db = db;
        this.current = current;
        this.inventario = inventario;
    }

    // ------------------------------------------------------------------ búsqueda de fórmulas

    /** Fórmulas confirmadas pendientes (o en proceso) de despacho; búsqueda por fórmula, cita, mascota o propietario. */
    public List<Map<String, Object>> pendientes(String q, String estado) {
        StringBuilder sql = new StringBuilder("""
                SELECT f.id, f.cita_id, f.estado_despacho, ed.nombre AS estado_despacho_nombre, f.fecha_confirmacion,
                       ma.nombre_mascota, CONCAT(cl.nombres,' ',cl.apellidos) AS propietario,
                       cl.numero_documento AS propietario_documento, CONCAT(me.nombres,' ',me.apellidos) AS medico,
                       (SELECT COUNT(*) FROM formula_medica_detalle d WHERE d.formula_id = f.id) AS items,
                       (SELECT COALESCE(SUM(d.cantidad_prescrita - d.cantidad_dispensada),0)
                          FROM formula_medica_detalle d WHERE d.formula_id = f.id) AS unidades_pendientes
                  FROM formula_medica f
                  JOIN estado_despacho ed ON ed.codigo = f.estado_despacho
                  JOIN cita c ON c.id = f.cita_id
                  JOIN mascota ma ON ma.mascota_id = f.mascota_id
                  JOIN cliente cl ON cl.cliente_id = c.cliente_id
                  JOIN medico me ON me.id = f.medico_id
                 WHERE f.estado = 'CONFIRMADA'
                """);
        List<Object> args = new ArrayList<>();
        if (estado != null && !estado.isBlank()) {
            sql.append(" AND f.estado_despacho = ?");
            args.add(estado.trim().toUpperCase(Locale.ROOT));
        } else {
            sql.append(" AND f.estado_despacho IN ('PENDIENTE_DESPACHO','EN_PREPARACION','DESPACHO_PARCIAL','PENDIENTE_DISPONIBILIDAD')");
        }
        if (q != null && !q.isBlank()) {
            sql.append(" AND (CAST(f.id AS CHAR) = ? OR CAST(f.cita_id AS CHAR) = ? OR ma.nombre_mascota LIKE ? "
                    + "OR cl.nombres LIKE ? OR cl.apellidos LIKE ? OR cl.numero_documento LIKE ?)");
            String like = "%" + q.trim() + "%";
            args.add(q.trim());
            args.add(q.trim());
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }
        sql.append(" ORDER BY f.fecha_confirmacion, f.id LIMIT 300");
        return db.list(sql.toString(), args.toArray());
    }

    /** Detalle de la fórmula con disponibilidad por medicamento y lotes propuestos (FEFO). */
    public Map<String, Object> formula(long id) {
        Map<String, Object> f = db.must("La fórmula no existe.", """
                SELECT f.id, f.cita_id, f.estado, f.estado_despacho, ed.nombre AS estado_despacho_nombre, f.observaciones,
                       f.fecha_confirmacion, ma.nombre_mascota, CONCAT(cl.nombres,' ',cl.apellidos) AS propietario,
                       cl.numero_documento AS propietario_documento, CONCAT(me.nombres,' ',me.apellidos) AS medico
                  FROM formula_medica f
                  JOIN estado_despacho ed ON ed.codigo = f.estado_despacho
                  JOIN cita c ON c.id = f.cita_id
                  JOIN mascota ma ON ma.mascota_id = f.mascota_id
                  JOIN cliente cl ON cl.cliente_id = c.cliente_id
                  JOIN medico me ON me.id = f.medico_id
                 WHERE f.id = ? AND f.estado = 'CONFIRMADA'
                """, id);
        List<Map<String, Object>> items = db.list("""
                SELECT d.id, d.medicamento_id, m.codigo, m.nombre AS medicamento, m.activo, d.dosis, d.frecuencia,
                       d.duracion_dias, d.via_administracion, d.indicaciones, d.cantidad_prescrita, d.cantidad_dispensada,
                       (d.cantidad_prescrita - d.cantidad_dispensada) AS cantidad_pendiente
                  FROM formula_medica_detalle d JOIN medicamento m ON m.id = d.medicamento_id
                 WHERE d.formula_id = ? ORDER BY d.id
                """, id);
        for (Map<String, Object> it : items) {
            long medId = In.toLong(it.get("medicamentoId"));
            int pendiente = In.toInt(it.get("cantidadPendiente"));
            List<Map<String, Object>> lotes = db.list(LOTES_USABLES, medId);
            int disponible = 0;
            for (Map<String, Object> l : lotes) {
                disponible += In.toInt(l.get("cantidadDisponible"));
            }
            int vencidas = db.scalar(Integer.class, """
                    SELECT COALESCE(SUM(cantidad_disponible),0) FROM medicamento_lote
                     WHERE medicamento_id = ? AND activo = 1 AND fecha_vencimiento < CURDATE()
                    """, medId);
            it.put("disponible", disponible);
            it.put("unidadesVencidas", vencidas);
            int aDespachar = Math.min(pendiente, disponible);
            it.put("puedeDespachar", aDespachar);
            it.put("lotesPropuestos", asignarFefo(lotes, aDespachar));
            String alerta = null;
            if (pendiente == 0) {
                alerta = "Totalmente dispensado";
            } else if (disponible == 0) {
                alerta = vencidas > 0 ? "Sin stock vigente (hay " + vencidas + " unidades vencidas)" : "Sin stock";
            } else if (disponible < pendiente) {
                alerta = "Stock insuficiente: solo " + disponible + " de " + pendiente + " (despacho parcial)";
            }
            it.put("alerta", alerta);
        }
        f.put("items", items);
        f.put("despachos", db.list("""
                SELECT d.id, d.numero_comprobante, d.estado, d.fecha_creacion, d.fecha_confirmacion, u.username AS usuario
                  FROM despacho d JOIN usuario u ON u.id = d.usuario_id WHERE d.formula_id = ? ORDER BY d.id
                """, id));
        f.put("log", logFormula(id));
        return f;
    }

    public List<Map<String, Object>> logFormula(long formulaId) {
        return db.list("""
                SELECT l.id, l.despacho_id, l.estado_anterior, l.estado_nuevo, l.fecha, l.observacion, u.username AS usuario
                  FROM despacho_estado_log l JOIN usuario u ON u.id = l.usuario_id WHERE l.formula_id = ? ORDER BY l.id
                """, formulaId);
    }

    private List<Map<String, Object>> asignarFefo(List<Map<String, Object>> lotes, int cantidad) {
        List<Map<String, Object>> out = new ArrayList<>();
        int resto = cantidad;
        for (Map<String, Object> l : lotes) {
            if (resto <= 0) {
                break;
            }
            int tomar = Math.min(resto, In.toInt(l.get("cantidadDisponible")));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("loteId", l.get("id"));
            m.put("numeroLote", l.get("numeroLote"));
            m.put("fechaVencimiento", l.get("fechaVencimiento"));
            m.put("cantidad", tomar);
            out.add(m);
            resto -= tomar;
        }
        return out;
    }

    // ------------------------------------------------------------------ estados

    /** Marca la fórmula como "en preparación" (la toma el usuario de despacho). */
    @Transactional
    public Map<String, Object> preparar(long formulaId) {
        Map<String, Object> f = bloquearFormula(formulaId);
        String ed = String.valueOf(f.get("estadoDespacho"));
        if ("EN_PREPARACION".equals(ed)) {
            return formula(formulaId);
        }
        if (!DESPACHABLES.contains(ed)) {
            throw new BadRequestException("La fórmula no se puede preparar en estado " + ed + ".");
        }
        cambiarEstado(formulaId, ed, "EN_PREPARACION", null, "Inicio de preparación");
        return formula(formulaId);
    }

    /** Cancela el despacho de una fórmula sin unidades dispensadas (motivo obligatorio). */
    @Transactional
    public Map<String, Object> cancelar(long formulaId, Map<String, Object> b) {
        String motivo = In.req(b, "motivo", "Motivo de cancelación");
        Map<String, Object> f = bloquearFormula(formulaId);
        String ed = String.valueOf(f.get("estadoDespacho"));
        if (!DESPACHABLES.contains(ed) || "DESPACHO_PARCIAL".equals(ed)) {
            throw new BadRequestException("Solo se puede cancelar una fórmula sin unidades dispensadas (estado actual: " + ed + ").");
        }
        cambiarEstado(formulaId, ed, "CANCELADO", null, motivo);
        return formula(formulaId);
    }

    private Map<String, Object> bloquearFormula(long id) {
        return db.must("La fórmula no existe o no está confirmada.",
                "SELECT id, estado_despacho FROM formula_medica WHERE id = ? AND estado = 'CONFIRMADA' FOR UPDATE", id);
    }

    private void cambiarEstado(long formulaId, String anterior, String nuevo, Long despachoId, String obs) {
        db.update("UPDATE formula_medica SET estado_despacho = ?, fecha_actualizacion_registro = NOW() WHERE id = ?", nuevo, formulaId);
        db.update("""
                INSERT INTO despacho_estado_log (formula_id, despacho_id, estado_anterior, estado_nuevo, usuario_id, observacion)
                VALUES (?,?,?,?,?,?)
                """, formulaId, despachoId, anterior, nuevo, current.get().id(),
                obs == null ? null : (obs.length() > 255 ? obs.substring(0, 255) : obs));
    }

    // ------------------------------------------------------------------ confirmación

    /**
     * Confirma el despacho en una sola transacción: asigna lotes FEFO (sin vencidos), descuenta stock por lote,
     * registra movimientos SALIDA, actualiza lo dispensado y el estado de la fórmula.
     * Cuerpo opcional: {observaciones, items:[{formulaDetalleId, cantidad}]}; sin items despacha lo disponible.
     * Si una cantidad pedida supera lo disponible se rechaza (se puede reducir para un despacho parcial).
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> confirmar(long formulaId, Map<String, Object> body) {
        Map<String, Object> f = bloquearFormula(formulaId);
        String estadoAnterior = String.valueOf(f.get("estadoDespacho"));
        if (!DESPACHABLES.contains(estadoAnterior)) {
            throw new BadRequestException("La fórmula no está disponible para despacho (estado actual: " + estadoAnterior + ").",
                    HttpStatus.CONFLICT);
        }
        List<Map<String, Object>> detalles = db.list("""
                SELECT d.id, d.medicamento_id, m.nombre, m.activo, d.cantidad_prescrita, d.cantidad_dispensada
                  FROM formula_medica_detalle d JOIN medicamento m ON m.id = d.medicamento_id
                 WHERE d.formula_id = ? ORDER BY d.medicamento_id, d.id FOR UPDATE
                """, formulaId);
        Map<Long, Integer> pedido = new LinkedHashMap<>();
        boolean explicito = body != null && body.get("items") instanceof List<?>;
        if (explicito) {
            for (Object o : (List<Object>) body.get("items")) {
                Map<String, Object> it = (Map<String, Object>) o;
                long did = In.reqLng(it, "formulaDetalleId", "Ítem de la fórmula");
                Integer c = In.intg(it, "cantidad");
                if (c == null || c < 0) {
                    throw new BadRequestException("La cantidad a despachar debe ser un número entero mayor o igual a cero.");
                }
                if (pedido.put(did, c) != null) {
                    throw new BadRequestException("El ítem " + did + " está repetido en el despacho.");
                }
            }
            for (Long did : pedido.keySet()) {
                if (detalles.stream().noneMatch(d -> In.toLong(d.get("id")) == did)) {
                    throw new BadRequestException("El ítem " + did + " no pertenece a esta fórmula.");
                }
            }
        }

        long usuario = current.get().id();
        long despachoId = db.insert("INSERT INTO despacho (formula_id, estado, usuario_id, observaciones) VALUES (?, 'EN_PREPARACION', ?, ?)",
                formulaId, usuario, body == null ? null : In.str(body, "observaciones"));
        int totalEntregado = 0;
        TreeSet<Long> medicamentosAfectados = new TreeSet<>();
        List<Map<String, Object>> resumen = new ArrayList<>();

        for (Map<String, Object> d : detalles) {
            long did = In.toLong(d.get("id"));
            long medId = In.toLong(d.get("medicamentoId"));
            int pendiente = In.toInt(d.get("cantidadPrescrita")) - In.toInt(d.get("cantidadDispensada"));
            int quiere = explicito ? pedido.getOrDefault(did, 0) : pendiente;
            if (quiere > pendiente) {
                throw new BadRequestException("'" + d.get("nombre") + "': se pide despachar " + quiere
                        + " pero solo faltan " + pendiente + " por dispensar.");
            }
            if (quiere == 0) {
                continue;
            }
            List<Map<String, Object>> lotes = db.list(LOTES_USABLES + " FOR UPDATE", medId);
            int disponible = lotes.stream().mapToInt(l -> In.toInt(l.get("cantidadDisponible"))).sum();
            boolean activo = "1".equals(String.valueOf(d.get("activo"))) || "true".equals(String.valueOf(d.get("activo")));
            if (!activo) {
                disponible = 0;
            }
            int entregar = quiere;
            if (quiere > disponible) {
                if (explicito) {
                    throw new BadRequestException("Stock insuficiente de '" + d.get("nombre") + "': disponible vigente " + disponible
                            + ", solicitado " + quiere + ". Reduzca la cantidad para realizar un despacho parcial.", HttpStatus.CONFLICT);
                }
                entregar = disponible;
            }
            if (entregar == 0) {
                continue;
            }
            long ddId = db.insert("""
                    INSERT INTO despacho_detalle (despacho_id, formula_detalle_id, medicamento_id, cantidad_solicitada, cantidad_entregada)
                    VALUES (?,?,?,?,?)
                    """, despachoId, did, medId, pendiente, entregar);
            int resto = entregar;
            for (Map<String, Object> l : lotes) {
                if (resto == 0) {
                    break;
                }
                int tomar = Math.min(resto, In.toInt(l.get("cantidadDisponible")));
                long loteId = In.toLong(l.get("id"));
                long dllId = db.insert("INSERT INTO despacho_detalle_lote (despacho_detalle_id, lote_id, cantidad) VALUES (?,?,?)",
                        ddId, loteId, tomar);
                db.update("UPDATE medicamento_lote SET cantidad_disponible = cantidad_disponible - ? WHERE id = ?", tomar, loteId);
                int saldo = inventario.refrescarResumen(medId);
                inventario.registrarMovimiento(medId, loteId, "SALIDA", tomar, saldo, usuario, formulaId, despachoId, dllId, null,
                        "Despacho fórmula " + formulaId + " lote " + l.get("numeroLote"));
                resto -= tomar;
            }
            db.update("UPDATE formula_medica_detalle SET cantidad_dispensada = cantidad_dispensada + ? WHERE id = ?", entregar, did);
            medicamentosAfectados.add(medId);
            totalEntregado += entregar;
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("medicamento", d.get("nombre"));
            r.put("entregado", entregar);
            resumen.add(r);
        }

        if (totalEntregado == 0) {
            // No se pudo dispensar nada: no queda registro de despacho, la fórmula queda pendiente por disponibilidad
            db.update("DELETE FROM despacho WHERE id = ?", despachoId);
            if (!"PENDIENTE_DISPONIBILIDAD".equals(estadoAnterior)) {
                cambiarEstado(formulaId, estadoAnterior, "PENDIENTE_DISPONIBILIDAD", null,
                        "Sin existencias vigentes para dispensar");
            }
            Map<String, Object> out = formula(formulaId);
            out.put("mensaje", "No hay existencias vigentes para dispensar; la fórmula queda pendiente por disponibilidad.");
            return out;
        }

        String nuevoEstado = recalcularEstado(formulaId);
        String estadoDespacho = "DESPACHADO".equals(nuevoEstado) ? "DESPACHADO" : "DESPACHO_PARCIAL";
        db.update("UPDATE despacho SET estado = ?, fecha_confirmacion = NOW(), numero_comprobante = ? WHERE id = ?",
                estadoDespacho, String.format("DSP-%06d", despachoId), despachoId);
        cambiarEstado(formulaId, estadoAnterior, nuevoEstado, despachoId,
                "DESPACHADO".equals(nuevoEstado) ? "Despacho completo" : "Despacho parcial: " + totalEntregado + " unidades");
        Map<String, Object> out = detalleDespacho(despachoId);
        out.put("formulaEstado", nuevoEstado);
        return out;
    }

    /** DESPACHADO si todo está dispensado; DESPACHO_PARCIAL si algo; si nada, PENDIENTE_DESPACHO. */
    private String recalcularEstado(long formulaId) {
        Map<String, Object> s = db.one("""
                SELECT COALESCE(SUM(cantidad_prescrita),0) AS prescrita, COALESCE(SUM(cantidad_dispensada),0) AS dispensada
                  FROM formula_medica_detalle WHERE formula_id = ?
                """, formulaId);
        long presc = In.toLong(s.get("prescrita"));
        long disp = In.toLong(s.get("dispensada"));
        return disp == 0 ? "PENDIENTE_DESPACHO" : disp >= presc ? "DESPACHADO" : "DESPACHO_PARCIAL";
    }

    // ------------------------------------------------------------------ historial

    public List<Map<String, Object>> historial(String estado, LocalDate desde, LocalDate hasta, String q) {
        StringBuilder sql = new StringBuilder("""
                SELECT d.id, d.numero_comprobante, d.formula_id, d.estado, ed.nombre AS estado_nombre, d.fecha_creacion,
                       d.fecha_confirmacion, u.username AS usuario, ma.nombre_mascota,
                       CONCAT(cl.nombres,' ',cl.apellidos) AS propietario,
                       (SELECT COALESCE(SUM(x.cantidad_entregada),0) FROM despacho_detalle x WHERE x.despacho_id = d.id) AS unidades,
                       (SELECT COUNT(*) FROM despacho_reversion r WHERE r.despacho_id = d.id) AS revertido
                  FROM despacho d
                  JOIN estado_despacho ed ON ed.codigo = d.estado
                  JOIN usuario u ON u.id = d.usuario_id
                  JOIN formula_medica f ON f.id = d.formula_id
                  JOIN mascota ma ON ma.mascota_id = f.mascota_id
                  JOIN cita c ON c.id = f.cita_id
                  JOIN cliente cl ON cl.cliente_id = c.cliente_id
                 WHERE 1=1
                """);
        List<Object> args = new ArrayList<>();
        if (estado != null && !estado.isBlank()) {
            sql.append(" AND d.estado = ?");
            args.add(estado.trim().toUpperCase(Locale.ROOT));
        }
        if (desde != null) {
            sql.append(" AND d.fecha_creacion >= ?");
            args.add(desde.atStartOfDay());
        }
        if (hasta != null) {
            sql.append(" AND d.fecha_creacion < ?");
            args.add(hasta.plusDays(1).atStartOfDay());
        }
        if (q != null && !q.isBlank()) {
            sql.append(" AND (d.numero_comprobante LIKE ? OR ma.nombre_mascota LIKE ? OR cl.nombres LIKE ? OR cl.apellidos LIKE ? "
                    + "OR CAST(d.formula_id AS CHAR) = ?)");
            String like = "%" + q.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(q.trim());
        }
        sql.append(" ORDER BY d.id DESC LIMIT 500");
        return db.list(sql.toString(), args.toArray());
    }

    public Map<String, Object> detalleDespacho(long id) {
        Map<String, Object> d = db.must("El despacho no existe.", """
                SELECT d.id, d.numero_comprobante, d.formula_id, d.estado, ed.nombre AS estado_nombre, d.fecha_creacion,
                       d.fecha_confirmacion, d.observaciones, u.username AS usuario, f.cita_id, ma.nombre_mascota,
                       CONCAT(cl.nombres,' ',cl.apellidos) AS propietario, cl.numero_documento AS propietario_documento,
                       CONCAT(me.nombres,' ',me.apellidos) AS medico
                  FROM despacho d
                  JOIN estado_despacho ed ON ed.codigo = d.estado
                  JOIN usuario u ON u.id = d.usuario_id
                  JOIN formula_medica f ON f.id = d.formula_id
                  JOIN mascota ma ON ma.mascota_id = f.mascota_id
                  JOIN cita c ON c.id = f.cita_id
                  JOIN cliente cl ON cl.cliente_id = c.cliente_id
                  JOIN medico me ON me.id = f.medico_id
                 WHERE d.id = ?
                """, id);
        List<Map<String, Object>> items = db.list("""
                SELECT dd.id, dd.medicamento_id, m.codigo, m.nombre AS medicamento, dd.cantidad_solicitada, dd.cantidad_entregada
                  FROM despacho_detalle dd JOIN medicamento m ON m.id = dd.medicamento_id
                 WHERE dd.despacho_id = ? ORDER BY dd.id
                """, id);
        for (Map<String, Object> it : items) {
            it.put("lotes", db.list("""
                    SELECT x.id, x.lote_id, l.numero_lote, l.fecha_vencimiento, x.cantidad
                      FROM despacho_detalle_lote x JOIN medicamento_lote l ON l.id = x.lote_id
                     WHERE x.despacho_detalle_id = ? ORDER BY x.id
                    """, In.toLong(it.get("id"))));
        }
        d.put("items", items);
        d.put("reversion", db.one("""
                SELECT r.id, r.motivo, r.fecha, us.username AS solicita, ua.username AS autoriza
                  FROM despacho_reversion r JOIN usuario us ON us.id = r.usuario_solicita_id
                  JOIN usuario ua ON ua.id = r.usuario_autoriza_id WHERE r.despacho_id = ?
                """, id));
        return d;
    }

    // ------------------------------------------------------------------ reversión autorizada

    /**
     * Revierte un despacho: devuelve las unidades a sus lotes, registra movimientos REVERSION y deja la
     * trazabilidad. Exige motivo y credenciales de un ADMINISTRADOR (autorizador). No se permite si hay una factura
     * vigente que incluya lo dispensado.
     * Cuerpo: {motivo, adminUsername, adminPassword}
     */
    @Transactional
    public Map<String, Object> revertir(long despachoId, Map<String, Object> b) {
        String motivo = In.req(b, "motivo", "Motivo de la reversión");
        if (motivo.length() < 10) {
            throw new BadRequestException("Describa el motivo de la reversión (mínimo 10 caracteres).");
        }
        String adminUser = In.req(b, "adminUsername", "Usuario autorizador");
        String adminPass = In.req(b, "adminPassword", "Contraseña del autorizador");
        Map<String, Object> admin = db.one("""
                SELECT u.id FROM usuario u WHERE u.username = ? AND u.password_hash = ? AND u.activo = 1 AND u.rol = 1
                """, adminUser, PasswordUtil.generarMD5(adminPass));
        if (admin == null) {
            throw new BadRequestException("La autorización no es válida: se requieren credenciales de un administrador activo.",
                    HttpStatus.FORBIDDEN);
        }
        Map<String, Object> d = db.must("El despacho no existe.",
                "SELECT id, formula_id, estado FROM despacho WHERE id = ? FOR UPDATE", despachoId);
        String estado = String.valueOf(d.get("estado"));
        if (!estado.equals("DESPACHADO") && !estado.equals("DESPACHO_PARCIAL")) {
            throw new BadRequestException("Solo se puede revertir un despacho confirmado (estado actual: " + estado + ").",
                    HttpStatus.CONFLICT);
        }
        long formulaId = In.toLong(d.get("formulaId"));
        bloquearFormulaSinEstado(formulaId);
        long facturas = db.count("""
                SELECT COUNT(*) FROM factura_detalle fd JOIN factura f ON f.id = fd.factura_id
                  JOIN despacho_detalle dd ON dd.id = fd.despacho_detalle_id
                 WHERE dd.despacho_id = ? AND f.estado <> 'ANULADA'
                """, despachoId);
        if (facturas > 0) {
            throw new BadRequestException("El despacho está incluido en una factura vigente. Anule la factura antes de revertir el despacho.",
                    HttpStatus.CONFLICT);
        }
        long usuario = current.get().id();
        List<Map<String, Object>> lotes = db.list("""
                SELECT x.id, x.lote_id, x.cantidad, dd.medicamento_id, dd.formula_detalle_id, l.numero_lote
                  FROM despacho_detalle_lote x
                  JOIN despacho_detalle dd ON dd.id = x.despacho_detalle_id
                  JOIN medicamento_lote l ON l.id = x.lote_id
                 WHERE dd.despacho_id = ? ORDER BY x.lote_id FOR UPDATE
                """, despachoId);
        List<Long> meds = new ArrayList<>();
        for (Map<String, Object> x : lotes) {
            long medId = In.toLong(x.get("medicamentoId"));
            int cant = In.toInt(x.get("cantidad"));
            db.update("UPDATE medicamento_lote SET cantidad_disponible = cantidad_disponible + ? WHERE id = ?", cant, In.toLong(x.get("loteId")));
            db.update("UPDATE formula_medica_detalle SET cantidad_dispensada = cantidad_dispensada - ? WHERE id = ?",
                    cant, In.toLong(x.get("formulaDetalleId")));
            int saldo = inventario.refrescarResumen(medId);
            inventario.registrarMovimiento(medId, In.toLong(x.get("loteId")), "REVERSION", cant, saldo, usuario, formulaId,
                    despachoId, null, null, "Reversión despacho " + despachoId + " lote " + x.get("numeroLote"));
            meds.add(medId);
        }
        db.update("UPDATE despacho SET estado = 'CANCELADO' WHERE id = ?", despachoId);
        db.insert("INSERT INTO despacho_reversion (despacho_id, usuario_solicita_id, usuario_autoriza_id, motivo) VALUES (?,?,?,?)",
                despachoId, usuario, In.toLong(admin.get("id")), motivo);
        String anterior = db.scalar(String.class, "SELECT estado_despacho FROM formula_medica WHERE id = ?", formulaId);
        cambiarEstado(formulaId, anterior, recalcularEstado(formulaId), despachoId, "Reversión autorizada: " + motivo);
        return detalleDespacho(despachoId);
    }

    private void bloquearFormulaSinEstado(long id) {
        db.must("La fórmula no existe.", "SELECT id FROM formula_medica WHERE id = ? FOR UPDATE", id);
    }

    // ------------------------------------------------------------------ comprobante PDF

    @SuppressWarnings("unchecked")
    public byte[] comprobantePdf(long id) {
        Map<String, Object> d = detalleDespacho(id);
        Map<String, Object> clinica = db.one("SELECT * FROM clinica_config WHERE id = 1");
        return PdfKit.render(false, doc -> {
            PdfKit.header(doc, clinica, "COMPROBANTE DE DESPACHO " + PdfKit.nz(d.get("numeroComprobante")));
            PdfKit.line(doc, "Fórmula N.º", String.valueOf(d.get("formulaId")));
            PdfKit.line(doc, "Estado", String.valueOf(d.get("estadoNombre")));
            PdfKit.line(doc, "Fecha", String.valueOf(PdfKit.nz(d.get("fechaConfirmacion"))).replace('T', ' '));
            PdfKit.line(doc, "Propietario", d.get("propietario") + "  (Doc. " + PdfKit.nz(d.get("propietarioDocumento")) + ")");
            PdfKit.line(doc, "Mascota", String.valueOf(d.get("nombreMascota")));
            PdfKit.line(doc, "Prescribe", String.valueOf(d.get("medico")));
            PdfKit.line(doc, "Despachado por", String.valueOf(d.get("usuario")));
            PdfKit.section(doc, "Medicamentos dispensados");
            PdfPTable t = PdfKit.table(new float[]{1.4f, 3.4f, 1.8f, 1.8f, 1.2f, 1.2f},
                    "Código", "Medicamento", "Lote", "Vence", "Cant. lote", "Total ítem");
            for (Map<String, Object> it : (List<Map<String, Object>>) d.get("items")) {
                List<Map<String, Object>> ls = (List<Map<String, Object>>) it.get("lotes");
                boolean primero = true;
                for (Map<String, Object> l : ls) {
                    PdfKit.cell(t, primero ? it.get("codigo") : "", false);
                    PdfKit.cell(t, primero ? it.get("medicamento") : "", false);
                    PdfKit.cell(t, l.get("numeroLote"), false);
                    PdfKit.cell(t, PdfKit.nz(l.get("fechaVencimiento")), false);
                    PdfKit.cell(t, l.get("cantidad"), true);
                    PdfKit.cell(t, primero ? it.get("cantidadEntregada") + " de " + it.get("cantidadSolicitada") : "", true);
                    primero = false;
                }
            }
            doc.add(t);
            if (d.get("observaciones") != null) {
                PdfKit.line(doc, "Observaciones", String.valueOf(d.get("observaciones")));
            }
            if (d.get("reversion") != null) {
                Map<String, Object> r = (Map<String, Object>) d.get("reversion");
                PdfKit.line(doc, "REVERTIDO", r.get("motivo") + " (autorizó " + r.get("autoriza") + ")");
            }
            PdfKit.line(doc, "Generado", PdfKit.now());
        });
    }
}
