package com.uniminuto.clinica.modulos.facturacion;

import com.lowagie.text.pdf.PdfPTable;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.security.CurrentUser;
import com.uniminuto.clinica.support.Db;
import com.uniminuto.clinica.support.In;
import com.uniminuto.clinica.support.PdfKit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Facturación: una factura vigente por cita atendida, valores históricos copiados de la tarifa/precio,
 * solo medicamentos efectivamente dispensados, pagos parciales y anulación controlada con trazabilidad.
 */
@Service
public class FacturacionService {

    private static final List<String> METODOS = List.of("EFECTIVO", "TARJETA", "TRANSFERENCIA", "OTRO");

    private final Db db;
    private final CurrentUser current;

    public FacturacionService(Db db, CurrentUser current) {
        this.db = db;
        this.current = current;
    }

    // ------------------------------------------------------------------ consultas

    /** Citas atendidas que aún no tienen factura vigente, con el estado de despacho de su fórmula. */
    public List<Map<String, Object>> pendientesDeFacturar() {
        return db.list("""
                SELECT c.id AS cita_id, c.fecha_hora AS fecha, a.id AS atencion_id, ma.nombre_mascota,
                       CONCAT(cl.nombres,' ',cl.apellidos) AS propietario,
                       CONCAT(me.nombres,' ',me.apellidos) AS medico,
                       f.id AS formula_id, f.estado_despacho, ed.nombre AS estado_despacho_nombre,
                       CASE WHEN f.id IS NOT NULL AND f.estado_despacho IN ('PENDIENTE_DESPACHO','EN_PREPARACION')
                            THEN 0 ELSE 1 END AS listo_para_facturar
                  FROM cita c
                  JOIN atencion_medica a ON a.cita_id = c.id AND a.estado = 'FINALIZADA'
                  JOIN mascota ma ON ma.mascota_id = c.mascota_id
                  JOIN cliente cl ON cl.cliente_id = c.cliente_id
                  JOIN medico me ON me.id = c.medico_id
                  LEFT JOIN formula_medica f ON f.atencion_id = a.id AND f.estado = 'CONFIRMADA'
                  LEFT JOIN estado_despacho ed ON ed.codigo = f.estado_despacho
                 WHERE c.estado = 'ATENDIDA'
                   AND NOT EXISTS (SELECT 1 FROM factura x WHERE x.cita_id = c.id AND x.estado <> 'ANULADA')
                 ORDER BY c.fecha_hora DESC
                """);
    }

    /** Historial de facturas con filtros opcionales. */
    public List<Map<String, Object>> listar(Long clienteId, Long mascotaId, String estado, LocalDate desde, LocalDate hasta,
                                            String q) {
        StringBuilder sql = new StringBuilder("""
                SELECT f.id, f.numero, f.cita_id, f.fecha_generacion, f.subtotal, f.descuento, f.total_iva, f.total,
                       f.total_pagado, ROUND(f.total - f.total_pagado, 2) AS saldo, f.estado, f.cliente_id, f.mascota_id,
                       CONCAT(cl.nombres,' ',cl.apellidos) AS propietario, ma.nombre_mascota,
                       u.username AS usuario
                  FROM factura f
                  JOIN cliente cl ON cl.cliente_id = f.cliente_id
                  JOIN mascota ma ON ma.mascota_id = f.mascota_id
                  JOIN usuario u ON u.id = f.usuario_id
                 WHERE 1=1
                """);
        List<Object> args = new ArrayList<>();
        if (clienteId != null) {
            sql.append(" AND f.cliente_id = ?");
            args.add(clienteId);
        }
        if (mascotaId != null) {
            sql.append(" AND f.mascota_id = ?");
            args.add(mascotaId);
        }
        if (estado != null && !estado.isBlank()) {
            sql.append(" AND f.estado = ?");
            args.add(estado.trim().toUpperCase(Locale.ROOT));
        }
        if (desde != null) {
            sql.append(" AND f.fecha_generacion >= ?");
            args.add(desde.atStartOfDay());
        }
        if (hasta != null) {
            sql.append(" AND f.fecha_generacion < ?");
            args.add(hasta.plusDays(1).atStartOfDay());
        }
        if (q != null && !q.isBlank()) {
            sql.append(" AND (f.numero LIKE ? OR ma.nombre_mascota LIKE ? OR cl.nombres LIKE ? OR cl.apellidos LIKE ?)");
            String like = "%" + q.trim() + "%";
            for (int i = 0; i < 4; i++) {
                args.add(like);
            }
        }
        sql.append(" ORDER BY f.id DESC LIMIT 500");
        return db.list(sql.toString(), args.toArray());
    }

    public Map<String, Object> detalle(long id) {
        Map<String, Object> f = db.must("La factura no existe.", """
                SELECT f.id, f.numero, f.cita_id, f.atencion_id, f.cliente_id, f.mascota_id, f.medico_id,
                       f.fecha_generacion, f.subtotal, f.descuento, f.total_iva, f.total, f.total_pagado,
                       ROUND(f.total - f.total_pagado, 2) AS saldo, f.estado, f.fecha_anulacion, f.motivo_anulacion,
                       CONCAT(cl.nombres,' ',cl.apellidos) AS propietario, cl.numero_documento AS propietario_documento,
                       cl.telefono AS propietario_telefono, ma.nombre_mascota,
                       CONCAT(me.nombres,' ',me.apellidos) AS medico, es.nombre AS especialidad,
                       u.username AS usuario, ua.username AS usuario_anula, ud.username AS descuento_autorizado_por
                  FROM factura f
                  JOIN cliente cl ON cl.cliente_id = f.cliente_id
                  JOIN mascota ma ON ma.mascota_id = f.mascota_id
                  JOIN medico me ON me.id = f.medico_id
                  LEFT JOIN especializacion es ON es.id = f.especializacion_id
                  JOIN usuario u ON u.id = f.usuario_id
                  LEFT JOIN usuario ua ON ua.id = f.usuario_anula_id
                  LEFT JOIN usuario ud ON ud.id = f.descuento_autorizado_por
                 WHERE f.id = ?
                """, id);
        f.put("detalle", db.list("""
                SELECT id, tipo, descripcion, cantidad, valor_unitario, iva_porcentaje, subtotal, valor_iva, total
                  FROM factura_detalle WHERE factura_id = ? ORDER BY id
                """, id));
        f.put("pagos", db.list("""
                SELECT p.id, p.monto, p.metodo, p.referencia, p.fecha_pago, p.observacion, u.username AS usuario
                  FROM pago p JOIN usuario u ON u.id = p.usuario_id WHERE p.factura_id = ? ORDER BY p.id
                """, id));
        f.put("log", db.list("""
                SELECT l.id, l.accion, l.estado_anterior, l.estado_nuevo, l.fecha, l.detalle, u.username AS usuario
                  FROM factura_log l JOIN usuario u ON u.id = l.usuario_id WHERE l.factura_id = ? ORDER BY l.id
                """, id));
        return f;
    }

    // ------------------------------------------------------------------ generación

    /**
     * Genera la factura de una cita atendida. Cuerpo opcional: {descuento, motivoDescuento}.
     * Solo el ADMINISTRADOR puede aplicar descuentos.
     */
    @Transactional
    public Map<String, Object> generar(long citaId, Map<String, Object> b) {
        Map<String, Object> cita = db.must("La cita no existe.", """
                SELECT c.id, c.estado, c.cliente_id, c.mascota_id, c.medico_id, me.especializacion_id
                  FROM cita c JOIN medico me ON me.id = c.medico_id WHERE c.id = ? FOR UPDATE
                """, citaId);
        if (db.count("SELECT COUNT(*) FROM factura WHERE cita_id = ? AND estado <> 'ANULADA'", citaId) > 0) {
            throw new BadRequestException("La cita ya tiene una factura vigente; solo se permite una factura por cita atendida.",
                    HttpStatus.CONFLICT);
        }
        if (!"ATENDIDA".equals(cita.get("estado"))) {
            throw new BadRequestException("Solo se pueden facturar citas atendidas (estado actual: " + cita.get("estado") + ").");
        }
        Map<String, Object> atencion = db.one(
                "SELECT id FROM atencion_medica WHERE cita_id = ? AND estado = 'FINALIZADA'", citaId);
        if (atencion == null) {
            throw new BadRequestException("La atención médica de la cita no está finalizada.");
        }
        long atencionId = In.toLong(atencion.get("id"));

        Map<String, Object> formula = db.one(
                "SELECT id, estado_despacho FROM formula_medica WHERE atencion_id = ? AND estado = 'CONFIRMADA'", atencionId);
        if (formula != null) {
            String ed = String.valueOf(formula.get("estadoDespacho"));
            if (ed.equals("PENDIENTE_DESPACHO") || ed.equals("EN_PREPARACION")) {
                throw new BadRequestException("La fórmula médica aún no ha sido despachada. Complete el despacho antes de facturar, "
                        + "porque solo se facturan los medicamentos efectivamente dispensados.");
            }
        }

        List<Linea> lineas = new ArrayList<>();
        for (Map<String, Object> s : db.list("""
                SELECT s.tarifa_id, s.descripcion, s.cantidad, s.valor_unitario, s.iva_porcentaje, t.tipo
                  FROM atencion_servicio s JOIN tarifa t ON t.id = s.tarifa_id WHERE s.atencion_id = ? ORDER BY s.id
                """, atencionId)) {
            lineas.add(new Linea("CONSULTA".equals(s.get("tipo")) ? "CONSULTA" : "SERVICIO", In.toLong(s.get("tarifaId")), null,
                    null, String.valueOf(s.get("descripcion")), In.toInt(s.get("cantidad")),
                    In.toDec(s.get("valorUnitario")), In.toDec(s.get("ivaPorcentaje"))));
        }
        if (formula != null) {
            for (Map<String, Object> d : db.list("""
                    SELECT dd.id, dd.medicamento_id, dd.cantidad_entregada, m.nombre, m.codigo, m.precio_venta, m.iva_porcentaje
                      FROM despacho_detalle dd
                      JOIN despacho de ON de.id = dd.despacho_id AND de.estado IN ('DESPACHADO','DESPACHO_PARCIAL')
                      JOIN medicamento m ON m.id = dd.medicamento_id
                     WHERE de.formula_id = ? AND dd.cantidad_entregada > 0 ORDER BY dd.id
                    """, In.toLong(formula.get("id")))) {
                lineas.add(new Linea("MEDICAMENTO", null, In.toLong(d.get("medicamentoId")), In.toLong(d.get("id")),
                        d.get("nombre") + " (" + d.get("codigo") + ")", In.toInt(d.get("cantidadEntregada")),
                        In.toDec(d.get("precioVenta")), In.toDec(d.get("ivaPorcentaje"))));
            }
        }
        if (lineas.isEmpty()) {
            throw new BadRequestException("La cita no tiene servicios ni medicamentos dispensados para facturar.");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal iva = BigDecimal.ZERO;
        for (Linea l : lineas) {
            subtotal = subtotal.add(l.subtotal());
            iva = iva.add(l.iva());
        }
        BigDecimal bruto = subtotal.add(iva);

        BigDecimal descuento = (b == null ? null : In.dec(b, "descuento"));
        if (descuento == null) {
            descuento = BigDecimal.ZERO;
        }
        descuento = descuento.setScale(2, RoundingMode.HALF_UP);
        Long autorizadoPor = null;
        if (descuento.signum() < 0 || descuento.compareTo(bruto) > 0) {
            throw new BadRequestException("El descuento debe estar entre 0 y el valor de la factura (" + bruto + ").");
        }
        if (descuento.signum() > 0) {
            if (!current.get().is("ADMINISTRADOR")) {
                throw new BadRequestException("Solo el administrador puede autorizar descuentos.", HttpStatus.FORBIDDEN);
            }
            autorizadoPor = current.get().id();
        }
        BigDecimal total = bruto.subtract(descuento);
        String estado = total.signum() == 0 ? "PAGADA" : "PENDIENTE_PAGO";

        long usuario = current.get().id();
        long id = db.insert("""
                INSERT INTO factura (numero, cita_id, atencion_id, cliente_id, mascota_id, medico_id, especializacion_id,
                                     subtotal, descuento, total_iva, total, total_pagado, estado, usuario_id,
                                     descuento_autorizado_por)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,0,?,?,?)
                """, "TMP" + UUID.randomUUID().toString().substring(0, 12), citaId, atencionId, cita.get("clienteId"),
                cita.get("mascotaId"), cita.get("medicoId"), cita.get("especializacionId"), subtotal, descuento, iva, total,
                estado, usuario, autorizadoPor);
        db.update("UPDATE factura SET numero = ? WHERE id = ?", String.format("FAC-%06d", id), id);
        for (Linea l : lineas) {
            db.insert("""
                    INSERT INTO factura_detalle (factura_id, tipo, tarifa_id, medicamento_id, despacho_detalle_id, descripcion,
                                                 cantidad, valor_unitario, iva_porcentaje, subtotal, valor_iva, total)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                    """, id, l.tipo(), l.tarifaId(), l.medicamentoId(), l.despachoDetalleId(), l.descripcion(), l.cantidad(),
                    l.valor(), l.ivaPct(), l.subtotal(), l.iva(), l.subtotal().add(l.iva()));
        }
        db.update("UPDATE cita SET estado = 'FINALIZADA', fecha_modificacion = NOW() WHERE id = ?", citaId);
        log(id, "GENERADA", null, estado, usuario, "Factura generada desde la cita " + citaId);
        if (descuento.signum() > 0) {
            log(id, "DESCUENTO", estado, estado, usuario, "Descuento autorizado: " + descuento);
        }
        return detalle(id);
    }

    private record Linea(String tipo, Long tarifaId, Long medicamentoId, Long despachoDetalleId, String descripcion,
                         int cantidad, BigDecimal valor, BigDecimal ivaPct) {
        BigDecimal subtotal() {
            return valor.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal iva() {
            return subtotal().multiply(ivaPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
    }

    // ------------------------------------------------------------------ pagos

    /** Registra un pago (total o parcial): {monto, metodo, referencia, observacion}. */
    @Transactional
    public Map<String, Object> pagar(long facturaId, Map<String, Object> b) {
        Map<String, Object> f = bloquear(facturaId);
        String estado = String.valueOf(f.get("estado"));
        if ("ANULADA".equals(estado)) {
            throw new BadRequestException("No se pueden registrar pagos en una factura anulada.");
        }
        if ("PAGADA".equals(estado)) {
            throw new BadRequestException("La factura ya está pagada.");
        }
        BigDecimal monto = In.reqDec(b, "monto", "Monto").setScale(2, RoundingMode.HALF_UP);
        String metodo = In.str(b, "metodo") == null ? "EFECTIVO" : In.str(b, "metodo").toUpperCase(Locale.ROOT);
        if (!METODOS.contains(metodo)) {
            throw new BadRequestException("Método de pago inválido. Use: " + String.join(", ", METODOS) + ".");
        }
        BigDecimal total = In.toDec(f.get("total"));
        BigDecimal pagado = In.toDec(f.get("totalPagado"));
        BigDecimal saldo = total.subtract(pagado);
        if (monto.signum() <= 0) {
            throw new BadRequestException("El monto del pago debe ser mayor que cero.");
        }
        if (monto.compareTo(saldo) > 0) {
            throw new BadRequestException("El pago (" + monto + ") supera el saldo pendiente (" + saldo + ").");
        }
        long usuario = current.get().id();
        db.insert("INSERT INTO pago (factura_id, monto, metodo, referencia, usuario_id, observacion) VALUES (?,?,?,?,?,?)",
                facturaId, monto, metodo, In.str(b, "referencia"), usuario, In.str(b, "observacion"));
        BigDecimal nuevoPagado = pagado.add(monto);
        String nuevo = nuevoPagado.compareTo(total) >= 0 ? "PAGADA" : "PAGO_PARCIAL";
        db.update("UPDATE factura SET total_pagado = ?, estado = ? WHERE id = ?", nuevoPagado, nuevo, facturaId);
        log(facturaId, "PAGO", estado, nuevo, usuario, "Pago " + metodo + " por " + monto);
        return detalle(facturaId);
    }

    // ------------------------------------------------------------------ anulación

    /** Anulación controlada: solo ADMINISTRADOR, con motivo obligatorio; conserva pagos y trazabilidad. */
    @Transactional
    public Map<String, Object> anular(long facturaId, Map<String, Object> b) {
        if (!current.get().is("ADMINISTRADOR")) {
            throw new BadRequestException("Solo el administrador puede anular facturas.", HttpStatus.FORBIDDEN);
        }
        String motivo = In.req(b, "motivo", "Motivo de anulación");
        if (motivo.length() < 10) {
            throw new BadRequestException("Describa el motivo de la anulación (mínimo 10 caracteres).");
        }
        Map<String, Object> f = bloquear(facturaId);
        String estado = String.valueOf(f.get("estado"));
        if ("ANULADA".equals(estado)) {
            throw new BadRequestException("La factura ya está anulada.");
        }
        long usuario = current.get().id();
        db.update("""
                UPDATE factura SET estado = 'ANULADA', fecha_anulacion = NOW(), usuario_anula_id = ?, motivo_anulacion = ?
                 WHERE id = ?
                """, usuario, motivo, facturaId);
        // La cita vuelve a ATENDIDA: podrá facturarse nuevamente (la factura anulada se conserva como historial)
        db.update("UPDATE cita SET estado = 'ATENDIDA', fecha_modificacion = NOW() WHERE id = ? AND estado = 'FINALIZADA'",
                In.toLong(f.get("citaId")));
        BigDecimal pagado = In.toDec(f.get("totalPagado"));
        log(facturaId, "ANULADA", estado, "ANULADA", usuario, motivo
                + (pagado.signum() > 0 ? " | Pagos registrados por " + pagado + ": requiere devolución/ajuste manual" : ""));
        return detalle(facturaId);
    }

    private Map<String, Object> bloquear(long id) {
        return db.must("La factura no existe.",
                "SELECT id, cita_id, total, total_pagado, estado FROM factura WHERE id = ? FOR UPDATE", id);
    }

    private void log(long facturaId, String accion, String anterior, String nuevo, long usuario, String detalle) {
        db.update("""
                INSERT INTO factura_log (factura_id, accion, estado_anterior, estado_nuevo, usuario_id, detalle)
                VALUES (?,?,?,?,?,?)
                """, facturaId, accion, anterior, nuevo, usuario, detalle.length() > 255 ? detalle.substring(0, 255) : detalle);
    }

    // ------------------------------------------------------------------ PDF

    @SuppressWarnings("unchecked")
    public byte[] pdf(long id) {
        Map<String, Object> f = detalle(id);
        Map<String, Object> clinica = db.one("SELECT * FROM clinica_config WHERE id = 1");
        return PdfKit.render(false, doc -> {
            PdfKit.header(doc, clinica, "FACTURA " + f.get("numero") + ("ANULADA".equals(f.get("estado")) ? "  – ANULADA" : ""));
            PdfKit.line(doc, "Fecha", String.valueOf(f.get("fechaGeneracion")).replace('T', ' '));
            PdfKit.line(doc, "Estado", String.valueOf(f.get("estado")).replace('_', ' '));
            PdfKit.line(doc, "Cliente", f.get("propietario") + "  (Doc. " + PdfKit.nz(f.get("propietarioDocumento")) + ")");
            PdfKit.line(doc, "Mascota", String.valueOf(f.get("nombreMascota")));
            PdfKit.line(doc, "Veterinario", f.get("medico") + " (" + PdfKit.nz(f.get("especialidad")) + ")");
            PdfKit.section(doc, "Detalle");
            PdfPTable t = PdfKit.table(new float[]{1.6f, 4f, 1f, 1.6f, 1f, 1.5f, 1.8f},
                    "Tipo", "Descripción", "Cant.", "Valor unit.", "IVA %", "IVA", "Total");
            for (Map<String, Object> d : (List<Map<String, Object>>) f.get("detalle")) {
                PdfKit.cell(t, d.get("tipo"), false);
                PdfKit.cell(t, d.get("descripcion"), false);
                PdfKit.cell(t, d.get("cantidad"), true);
                PdfKit.cell(t, PdfKit.money(d.get("valorUnitario")), true);
                PdfKit.cell(t, d.get("ivaPorcentaje"), true);
                PdfKit.cell(t, PdfKit.money(d.get("valorIva")), true);
                PdfKit.cell(t, PdfKit.money(d.get("total")), true);
            }
            doc.add(t);
            PdfKit.line(doc, "Subtotal", PdfKit.money(f.get("subtotal")));
            PdfKit.line(doc, "IVA", PdfKit.money(f.get("totalIva")));
            PdfKit.line(doc, "Descuento", PdfKit.money(f.get("descuento")));
            PdfKit.line(doc, "TOTAL", PdfKit.money(f.get("total")));
            PdfKit.line(doc, "Pagado", PdfKit.money(f.get("totalPagado")));
            PdfKit.line(doc, "Saldo", PdfKit.money(f.get("saldo")));
            List<Map<String, Object>> pagos = (List<Map<String, Object>>) f.get("pagos");
            if (!pagos.isEmpty()) {
                PdfKit.section(doc, "Pagos");
                PdfPTable p = PdfKit.table(new float[]{2.5f, 2f, 2f, 3f}, "Fecha", "Método", "Monto", "Referencia");
                for (Map<String, Object> pg : pagos) {
                    PdfKit.cell(p, String.valueOf(pg.get("fechaPago")).replace('T', ' '), false);
                    PdfKit.cell(p, pg.get("metodo"), false);
                    PdfKit.cell(p, PdfKit.money(pg.get("monto")), true);
                    PdfKit.cell(p, PdfKit.nz(pg.get("referencia")), false);
                }
                doc.add(p);
            }
            if ("ANULADA".equals(f.get("estado"))) {
                PdfKit.line(doc, "Motivo de anulación", String.valueOf(f.get("motivoAnulacion")));
            }
            PdfKit.line(doc, "Generado", PdfKit.now());
        });
    }
}
