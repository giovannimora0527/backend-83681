package com.uniminuto.clinica.modulos.inventario;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.security.CurrentUser;
import com.uniminuto.clinica.support.Db;
import com.uniminuto.clinica.support.In;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Módulo 1 - Inventario: medicamentos/productos, lotes, entradas, bajas y kárdex (movimientos).
 * Regla central: las existencias viven en medicamento_lote; medicamento.stock_actual es el
 * consolidado que se recalcula dentro de la misma transacción de cada movimiento.
 */
@Service
public class InventarioService {

    public static final Pattern CODIGO = Pattern.compile("^[A-Za-z0-9]{8}$");

    /** Consulta base con las existencias y banderas de estado de cada medicamento. */
    private static final String BASE = """
            SELECT m.id, m.codigo, m.tipo, m.nombre, m.descripcion, m.presentacion, m.requiere_invima,
                   m.registro_invima, m.precio_compra, m.precio_venta, m.iva_porcentaje, m.stock_actual,
                   m.stock_minimo, m.fecha_ingreso, m.fecha_proximo_pedido, m.activo,
                   (SELECT COALESCE(SUM(l.cantidad_disponible),0) FROM medicamento_lote l
                     WHERE l.medicamento_id = m.id AND l.activo = 1
                       AND (l.fecha_vencimiento IS NULL OR l.fecha_vencimiento >= CURDATE())) AS stock_dispensable,
                   (SELECT COALESCE(SUM(l.cantidad_disponible),0) FROM medicamento_lote l
                     WHERE l.medicamento_id = m.id AND l.activo = 1
                       AND l.fecha_vencimiento < CURDATE()) AS stock_vencido,
                   (SELECT MIN(l.fecha_vencimiento) FROM medicamento_lote l
                     WHERE l.medicamento_id = m.id AND l.activo = 1 AND l.cantidad_disponible > 0
                       AND l.fecha_vencimiento IS NOT NULL AND l.fecha_vencimiento >= CURDATE()) AS proxima_fecha_vence,
                   (SELECT COUNT(*) FROM medicamento_lote l
                     WHERE l.medicamento_id = m.id AND l.activo = 1 AND l.cantidad_disponible > 0) AS lotes_activos
              FROM medicamento m
            """;

    private final Db db;
    private final CurrentUser current;
    private final int diasPorVencer;

    public InventarioService(Db db, CurrentUser current,
                             @Value("${app.inventario.dias-por-vencer:60}") int diasPorVencer) {
        this.db = db;
        this.current = current;
        this.diasPorVencer = diasPorVencer;
    }

    // ------------------------------------------------------------------ consultas

    /** Lista medicamentos/productos con banderas: agotado, stockBajo, porVencer, vencido. */
    public List<Map<String, Object>> listar(String q, String estado, String tipo, boolean soloActivos) {
        StringBuilder sql = new StringBuilder("SELECT * FROM (" + BASE + ") x WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append(" AND (x.nombre LIKE ? OR x.codigo LIKE ? OR x.registro_invima LIKE ?)");
            String like = "%" + q.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        if (tipo != null && !tipo.isBlank()) {
            sql.append(" AND x.tipo = ?");
            args.add(tipo.trim().toUpperCase(Locale.ROOT));
        }
        if (soloActivos) {
            sql.append(" AND x.activo = 1");
        }
        if (estado != null && !estado.isBlank()) {
            switch (estado.trim().toUpperCase(Locale.ROOT)) {
                case "AGOTADO" -> sql.append(" AND x.stock_actual = 0");
                case "STOCK_BAJO" -> sql.append(" AND x.stock_actual > 0 AND x.stock_actual <= x.stock_minimo");
                case "POR_VENCER" -> {
                    sql.append(" AND x.proxima_fecha_vence IS NOT NULL AND x.proxima_fecha_vence <= DATE_ADD(CURDATE(), INTERVAL ? DAY)");
                    args.add(diasPorVencer);
                }
                case "VENCIDO" -> sql.append(" AND x.stock_vencido > 0");
                case "DISPONIBLE" -> sql.append(" AND x.stock_dispensable > 0");
                default -> throw new BadRequestException("Estado de inventario no válido: " + estado);
            }
        }
        sql.append(" ORDER BY x.nombre");
        List<Map<String, Object>> rows = db.list(sql.toString(), args.toArray());
        rows.forEach(this::agregarBanderas);
        return rows;
    }

    public Map<String, Object> obtener(long id) {
        Map<String, Object> m = db.must("El medicamento no existe.", "SELECT * FROM (" + BASE + ") x WHERE x.id = ?", id);
        agregarBanderas(m);
        return m;
    }

    private void agregarBanderas(Map<String, Object> m) {
        long stock = In.toLong(m.get("stockActual"));
        long minimo = In.toLong(m.get("stockMinimo"));
        m.put("agotado", stock == 0);
        m.put("stockBajo", stock > 0 && stock <= minimo);
        m.put("vencido", In.toLong(m.get("stockVencido")) > 0);
        Object prox = m.get("proximaFechaVence");
        boolean porVencer = false;
        if (prox != null) {
            LocalDate f = LocalDate.parse(String.valueOf(prox));
            porVencer = !f.isAfter(LocalDate.now().plusDays(diasPorVencer));
        }
        m.put("porVencer", porVencer);
        m.put("diasPorVencer", diasPorVencer);
    }

    /** Resumen para el tablero de alertas del inventario. */
    public Map<String, Object> resumen() {
        List<Map<String, Object>> todos = listar(null, null, null, true);
        long agotados = todos.stream().filter(m -> Boolean.TRUE.equals(m.get("agotado"))).count();
        long bajos = todos.stream().filter(m -> Boolean.TRUE.equals(m.get("stockBajo"))).count();
        long porVencer = todos.stream().filter(m -> Boolean.TRUE.equals(m.get("porVencer"))).count();
        long vencidos = todos.stream().filter(m -> Boolean.TRUE.equals(m.get("vencido"))).count();
        return Map.of("total", todos.size(), "agotados", agotados, "stockBajo", bajos,
                "porVencer", porVencer, "vencidos", vencidos, "diasPorVencer", diasPorVencer);
    }

    public List<Map<String, Object>> lotes(long medicamentoId) {
        obtener(medicamentoId);
        List<Map<String, Object>> rows = db.list("""
                SELECT l.id, l.numero_lote, l.fecha_ingreso, l.fecha_vencimiento, l.precio_compra,
                       l.cantidad_inicial, l.cantidad_disponible, l.activo,
                       (l.fecha_vencimiento IS NOT NULL AND l.fecha_vencimiento < CURDATE()) AS vencido,
                       (l.fecha_vencimiento IS NOT NULL AND l.fecha_vencimiento >= CURDATE()
                          AND l.fecha_vencimiento <= DATE_ADD(CURDATE(), INTERVAL ? DAY)) AS por_vencer
                  FROM medicamento_lote l
                 WHERE l.medicamento_id = ?
                 ORDER BY (l.fecha_vencimiento IS NULL), l.fecha_vencimiento, l.id
                """, diasPorVencer, medicamentoId);
        return rows;
    }

    /** Kárdex: historial de entradas y salidas, con la fórmula/despacho/mascota relacionados. */
    public List<Map<String, Object>> movimientos(long medicamentoId, LocalDate inicio, LocalDate fin, String tipo) {
        obtener(medicamentoId);
        StringBuilder sql = new StringBuilder("""
                SELECT mv.id, mv.fecha, mv.tipo, mv.cantidad, mv.saldo_posterior, mv.observacion,
                       u.username AS usuario, l.numero_lote, mv.formula_id, mv.despacho_id, mv.carga_id,
                       d.numero_comprobante, ma.nombre_mascota,
                       CONCAT(c.nombres, ' ', c.apellidos) AS propietario
                  FROM inventario_movimiento mv
                  JOIN usuario u ON u.id = mv.usuario_id
                  LEFT JOIN medicamento_lote l ON l.id = mv.lote_id
                  LEFT JOIN despacho d ON d.id = mv.despacho_id
                  LEFT JOIN formula_medica f ON f.id = mv.formula_id
                  LEFT JOIN mascota ma ON ma.mascota_id = f.mascota_id
                  LEFT JOIN cliente c ON c.cliente_id = ma.cliente_id
                 WHERE mv.medicamento_id = ?
                """);
        List<Object> args = new ArrayList<>();
        args.add(medicamentoId);
        if (inicio != null) {
            sql.append(" AND mv.fecha >= ?");
            args.add(inicio.atStartOfDay());
        }
        if (fin != null) {
            sql.append(" AND mv.fecha < ?");
            args.add(fin.plusDays(1).atStartOfDay());
        }
        if (tipo != null && !tipo.isBlank()) {
            sql.append(" AND mv.tipo = ?");
            args.add(tipo.trim().toUpperCase(Locale.ROOT));
        }
        sql.append(" ORDER BY mv.fecha DESC, mv.id DESC");
        return db.list(sql.toString(), args.toArray());
    }

    // ------------------------------------------------------------------ escritura

    /** Valores ya validados de un medicamento. */
    public record Datos(String codigo, String tipo, String nombre, String descripcion, String presentacion,
                        boolean requiereInvima, String registroInvima, BigDecimal precioCompra,
                        BigDecimal precioVenta, BigDecimal iva, int stockMinimo, LocalDate proximoPedido) {
    }

    /**
     * Valida los datos de un medicamento/producto (alta, edición y carga masiva usan la misma regla).
     * Lanza BadRequestException con el primer problema encontrado.
     */
    public Datos validar(Map<String, ?> b, Long idExcluir) {
        String nombre = In.req(b, "nombre", "Nombre");
        if (nombre.length() > 100) {
            throw new BadRequestException("El nombre no puede superar 100 caracteres.");
        }
        String tipo = In.str(b, "tipo") == null ? "MEDICAMENTO" : In.str(b, "tipo").toUpperCase(Locale.ROOT);
        if (!tipo.equals("MEDICAMENTO") && !tipo.equals("PRODUCTO")) {
            throw new BadRequestException("El tipo debe ser MEDICAMENTO o PRODUCTO.");
        }
        // Los medicamentos siempre exigen Invima; los utensilios/productos pueden no tenerlo.
        boolean requiere = tipo.equals("MEDICAMENTO") || In.bool(b, "requiereInvima", false);
        String invima = In.str(b, "registroInvima");
        if (invima != null && invima.length() > 50) {
            throw new BadRequestException("El registro Invima no puede superar 50 caracteres.");
        }
        if (requiere && invima == null) {
            throw new BadRequestException("El registro Invima es obligatorio para medicamentos y productos que lo requieren.");
        }
        String presentacion = In.str(b, "presentacion");
        if (presentacion != null && presentacion.length() > 100) {
            throw new BadRequestException("La presentación no puede superar 100 caracteres.");
        }
        BigDecimal compra = In.reqDec(b, "precioCompra", "Precio de compra");
        BigDecimal venta = In.reqDec(b, "precioVenta", "Precio de venta");
        BigDecimal iva = In.dec(b, "ivaPorcentaje") == null ? BigDecimal.ZERO : In.dec(b, "ivaPorcentaje");
        if (compra.signum() < 0 || venta.signum() < 0) {
            throw new BadRequestException("Los precios no pueden ser negativos.");
        }
        if (venta.compareTo(compra) < 0) {
            throw new BadRequestException("El precio de venta al público no puede ser menor al precio de compra.");
        }
        if (iva.signum() < 0 || iva.compareTo(new BigDecimal("100")) > 0) {
            throw new BadRequestException("El IVA debe estar entre 0 y 100.");
        }
        Integer minimo = In.intg(b, "stockMinimo");
        if (minimo == null) {
            minimo = 0;
        }
        if (minimo < 0) {
            throw new BadRequestException("El stock mínimo no puede ser negativo.");
        }
        String codigo = In.str(b, "codigo");
        if (codigo != null) {
            codigo = codigo.toUpperCase(Locale.ROOT);
            if (!CODIGO.matcher(codigo).matches()) {
                throw new BadRequestException("El código debe tener exactamente 8 caracteres alfanuméricos.");
            }
            long dup = db.count("SELECT COUNT(*) FROM medicamento WHERE codigo = ? AND id <> ?",
                    codigo, idExcluir == null ? -1L : idExcluir);
            if (dup > 0) {
                throw new BadRequestException("Ya existe un medicamento con el código " + codigo + ".");
            }
        }
        long dupNombre = db.count("SELECT COUNT(*) FROM medicamento WHERE LOWER(nombre) = LOWER(?) AND id <> ?",
                nombre, idExcluir == null ? -1L : idExcluir);
        if (dupNombre > 0) {
            throw new BadRequestException("Ya existe un medicamento/producto con el nombre '" + nombre + "'.");
        }
        return new Datos(codigo, tipo, nombre, In.str(b, "descripcion"), presentacion, requiere, invima,
                compra, venta, iva, minimo, In.date(b, "fechaProximoPedido"));
    }

    /** Genera un código de 8 caracteres (MED + 5 dígitos) que no exista todavía. */
    public String generarCodigo() {
        long n = db.count("SELECT COALESCE(MAX(id),0) FROM medicamento") + 1;
        for (int i = 0; i < 1000; i++) {
            String c = String.format("MED%05d", n + i);
            if (db.count("SELECT COUNT(*) FROM medicamento WHERE codigo = ?", c) == 0) {
                return c;
            }
        }
        throw new BadRequestException("No fue posible generar un código único.");
    }

    @Transactional
    public Map<String, Object> crear(Map<String, Object> body) {
        Datos d = validar(body, null);
        long id = insertarMedicamento(d, In.date(body, "fechaIngreso"));
        Integer inicial = In.intg(body, "cantidadInicial");
        if (inicial != null && inicial > 0) {
            Map<String, Object> entrada = new java.util.HashMap<>(body);
            entrada.put("cantidad", inicial);
            registrarEntrada(id, entrada, null);
        }
        return obtener(id);
    }

    /** INSERT del medicamento; compartido con la carga masiva. */
    public long insertarMedicamento(Datos d, LocalDate fechaIngreso) {
        String codigo = d.codigo() == null ? generarCodigo() : d.codigo();
        LocalDate ingreso = fechaIngreso == null ? LocalDate.now() : fechaIngreso;
        return db.insert("""
                INSERT INTO medicamento (codigo, tipo, nombre, descripcion, presentacion, requiere_invima, registro_invima,
                                         precio_compra, precio_venta, iva_porcentaje, stock_actual, stock_minimo,
                                         fecha_ingreso, fecha_proximo_pedido, fecha_compra, activo, fecha_creacion_registro)
                VALUES (?,?,?,?,?,?,?,?,?,?,0,?,?,?,?,1,NOW())
                """, codigo, d.tipo(), d.nombre(), d.descripcion(), d.presentacion(), d.requiereInvima(),
                d.registroInvima(), d.precioCompra(), d.precioVenta(), d.iva(), d.stockMinimo(),
                ingreso, d.proximoPedido(), ingreso);
    }

    @Transactional
    public Map<String, Object> actualizar(long id, Map<String, Object> body) {
        obtener(id);
        // Para editar, el código existente se conserva si no se envía uno nuevo.
        if (In.str(body, "codigo") == null) {
            body.put("codigo", db.scalar(String.class, "SELECT codigo FROM medicamento WHERE id = ?", id));
        }
        Datos d = validar(body, id);
        db.update("""
                UPDATE medicamento SET codigo=?, tipo=?, nombre=?, descripcion=?, presentacion=?, requiere_invima=?,
                       registro_invima=?, precio_compra=?, precio_venta=?, iva_porcentaje=?, stock_minimo=?,
                       fecha_proximo_pedido=?, fecha_modificacion_registro=NOW()
                 WHERE id=?
                """, d.codigo(), d.tipo(), d.nombre(), d.descripcion(), d.presentacion(), d.requiereInvima(),
                d.registroInvima(), d.precioCompra(), d.precioVenta(), d.iva(), d.stockMinimo(), d.proximoPedido(), id);
        return obtener(id);
    }

    @Transactional
    public Map<String, Object> cambiarEstado(long id, boolean activo) {
        obtener(id);
        db.update("UPDATE medicamento SET activo = ?, fecha_modificacion_registro = NOW() WHERE id = ?", activo, id);
        return obtener(id);
    }

    /** Entrada de inventario: crea un lote nuevo y registra el movimiento ENTRADA. */
    @Transactional
    public Map<String, Object> entrada(long medicamentoId, Map<String, Object> body) {
        return registrarEntrada(medicamentoId, body, null);
    }

    /**
     * Crea el lote y el movimiento ENTRADA. {@code cargaId} identifica el cargue masivo de origen (puede ser null).
     * Debe invocarse dentro de una transacción.
     */
    public Map<String, Object> registrarEntrada(long medicamentoId, Map<String, ?> body, Long cargaId) {
        Map<String, Object> med = db.must("El medicamento no existe.",
                "SELECT id, tipo, precio_compra FROM medicamento WHERE id = ? FOR UPDATE", medicamentoId);
        int cantidad = In.intg(body, "cantidad") == null ? 0 : In.intg(body, "cantidad");
        if (cantidad <= 0) {
            throw new BadRequestException("La cantidad de la entrada debe ser mayor a cero.");
        }
        String lote = In.str(body, "numeroLote");
        if (lote == null) {
            lote = "L" + java.time.LocalDate.now().toString().replace("-", "") + "-" + medicamentoId;
        }
        if (lote.length() > 50) {
            throw new BadRequestException("El número de lote no puede superar 50 caracteres.");
        }
        LocalDate ingreso = In.date(body, "fechaIngreso") == null ? LocalDate.now() : In.date(body, "fechaIngreso");
        if (ingreso.isAfter(LocalDate.now())) {
            throw new BadRequestException("La fecha de ingreso no puede ser futura.");
        }
        LocalDate vence = In.date(body, "fechaVencimiento");
        boolean esMedicamento = "MEDICAMENTO".equals(med.get("tipo"));
        if (vence == null && esMedicamento) {
            throw new BadRequestException("La fecha de vencimiento es obligatoria para medicamentos.");
        }
        if (vence != null && !vence.isAfter(LocalDate.now())) {
            throw new BadRequestException("No se puede ingresar un lote ya vencido (" + vence + ").");
        }
        if (vence != null && vence.isBefore(ingreso)) {
            throw new BadRequestException("La fecha de vencimiento no puede ser anterior a la de ingreso.");
        }
        BigDecimal precio = In.dec(body, "precioCompra") == null ? In.toDec(med.get("precioCompra")) : In.dec(body, "precioCompra");
        if (precio.signum() < 0) {
            throw new BadRequestException("El precio de compra no puede ser negativo.");
        }
        if (db.count("SELECT COUNT(*) FROM medicamento_lote WHERE medicamento_id = ? AND numero_lote = ?",
                medicamentoId, lote) > 0) {
            throw new BadRequestException("El lote '" + lote + "' ya existe para este medicamento.");
        }
        long loteId = db.insert("""
                INSERT INTO medicamento_lote (medicamento_id, numero_lote, fecha_ingreso, fecha_vencimiento,
                                              precio_compra, cantidad_inicial, cantidad_disponible)
                VALUES (?,?,?,?,?,?,?)
                """, medicamentoId, lote, ingreso, vence, precio, cantidad, cantidad);
        int saldo = refrescarResumen(medicamentoId);
        db.update("UPDATE medicamento SET precio_compra = ?, fecha_ingreso = ? WHERE id = ?", precio, ingreso, medicamentoId);
        registrarMovimiento(medicamentoId, loteId, "ENTRADA", cantidad, saldo, current.get().id(),
                null, null, null, cargaId, cargaId == null ? "Entrada de inventario - lote " + lote : "Carga masiva #" + cargaId);
        return Map.of("loteId", loteId, "saldo", saldo);
    }

    /** Baja de unidades de un lote (vencimiento, daño). Genera un movimiento AJUSTE. */
    @Transactional
    public Map<String, Object> bajaLote(long loteId, Map<String, Object> body) {
        Map<String, Object> lote = db.must("El lote no existe.",
                "SELECT id, medicamento_id, numero_lote, cantidad_disponible FROM medicamento_lote WHERE id = ? FOR UPDATE", loteId);
        int cantidad = In.intg(body, "cantidad") == null ? 0 : In.intg(body, "cantidad");
        String motivo = In.req(body, "motivo", "Motivo de la baja");
        int disponible = In.toInt(lote.get("cantidadDisponible"));
        if (cantidad <= 0 || cantidad > disponible) {
            throw new BadRequestException("La cantidad a dar de baja debe estar entre 1 y " + disponible + ".");
        }
        long medId = In.toLong(lote.get("medicamentoId"));
        db.update("UPDATE medicamento_lote SET cantidad_disponible = cantidad_disponible - ? WHERE id = ?", cantidad, loteId);
        int saldo = refrescarResumen(medId);
        registrarMovimiento(medId, loteId, "AJUSTE", cantidad, saldo, current.get().id(), null, null, null, null,
                "BAJA lote " + lote.get("numeroLote") + ": " + motivo);
        return obtener(medId);
    }

    // ------------------------------------------------------------------ utilidades compartidas con despacho/carga

    /** Recalcula stock_actual y fecha_vence a partir de los lotes. Devuelve el nuevo stock. */
    public int refrescarResumen(long medicamentoId) {
        db.update("""
                UPDATE medicamento m SET
                   m.stock_actual = (SELECT COALESCE(SUM(l.cantidad_disponible),0) FROM medicamento_lote l
                                      WHERE l.medicamento_id = m.id AND l.activo = 1),
                   m.fecha_vence  = (SELECT MIN(l.fecha_vencimiento) FROM medicamento_lote l
                                      WHERE l.medicamento_id = m.id AND l.activo = 1
                                        AND l.cantidad_disponible > 0 AND l.fecha_vencimiento IS NOT NULL),
                   m.fecha_modificacion_registro = NOW()
                 WHERE m.id = ?
                """, medicamentoId);
        Integer s = db.scalar(Integer.class, "SELECT stock_actual FROM medicamento WHERE id = ?", medicamentoId);
        return s == null ? 0 : s;
    }

    public long registrarMovimiento(long medicamentoId, Long loteId, String tipo, int cantidad, int saldo,
                                    long usuarioId, Long formulaId, Long despachoId, Long despachoDetalleLoteId,
                                    Long cargaId, String observacion) {
        return db.insert("""
                INSERT INTO inventario_movimiento (medicamento_id, lote_id, tipo, cantidad, saldo_posterior, usuario_id,
                                                   formula_id, despacho_id, despacho_detalle_lote_id, carga_id, observacion)
                VALUES (?,?,?,?,?,?,?,?,?,?,?)
                """, medicamentoId, loteId, tipo, cantidad, saldo, usuarioId, formulaId, despachoId,
                despachoDetalleLoteId, cargaId, observacion);
    }

    public int getDiasPorVencer() {
        return diasPorVencer;
    }

    /** Respuesta 403 estándar. */
    public static BadRequestException prohibido(String msg) {
        return new BadRequestException(msg, HttpStatus.FORBIDDEN);
    }
}
