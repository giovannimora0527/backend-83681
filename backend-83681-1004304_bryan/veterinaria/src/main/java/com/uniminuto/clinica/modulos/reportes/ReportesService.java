package com.uniminuto.clinica.modulos.reportes;

import com.lowagie.text.pdf.PdfPTable;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.security.CurrentUser;
import com.uniminuto.clinica.support.Db;
import com.uniminuto.clinica.support.In;
import com.uniminuto.clinica.support.PdfKit;
import com.uniminuto.clinica.support.XlsxKit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Reportes financieros, de inventario y de gestión médica. Cada reporte se calcula una sola vez
 * y se entrega como vista previa (JSON), PDF o Excel con el mismo contenido, filtros y totales.
 */
@Service
public class ReportesService {

    /** Columna: clave del dato, encabezado y tipo (T texto, N número, M dinero). */
    public record Col(String key, String header, char type) {
    }

    record Def(String titulo, String grupo, boolean usaFechas, List<Col> cols, List<String> sumar) {
    }

    private static final Map<String, Def> CATALOGO = new LinkedHashMap<>();

    static {
        CATALOGO.put("facturacion", new Def("Facturación por período", "FINANCIERO", true, List.of(
                new Col("numero", "Factura", 'T'), new Col("fecha", "Fecha", 'T'), new Col("propietario", "Cliente", 'T'),
                new Col("mascota", "Mascota", 'T'), new Col("medico", "Veterinario", 'T'), new Col("especialidad", "Especialidad", 'T'),
                new Col("estado", "Estado", 'T'), new Col("subtotal", "Subtotal", 'M'), new Col("iva", "IVA", 'M'),
                new Col("descuento", "Descuento", 'M'), new Col("total", "Total", 'M'), new Col("pagado", "Pagado", 'M'),
                new Col("saldo", "Saldo", 'M')),
                List.of("subtotal", "iva", "descuento", "total", "pagado", "saldo")));
        CATALOGO.put("ingresos", new Def("Ingresos (pagos recibidos)", "FINANCIERO", true, List.of(
                new Col("fecha", "Fecha", 'T'), new Col("factura", "Factura", 'T'), new Col("propietario", "Cliente", 'T'),
                new Col("metodo", "Método", 'T'), new Col("referencia", "Referencia", 'T'), new Col("usuario", "Registró", 'T'),
                new Col("monto", "Monto", 'M')),
                List.of("monto")));
        CATALOGO.put("ingresos-por-servicio", new Def("Ingresos por servicio / medicamento", "FINANCIERO", true, List.of(
                new Col("tipo", "Tipo", 'T'), new Col("descripcion", "Descripción", 'T'), new Col("cantidad", "Cantidad", 'N'),
                new Col("subtotal", "Subtotal", 'M'), new Col("iva", "IVA", 'M'), new Col("total", "Total", 'M')),
                List.of("cantidad", "subtotal", "iva", "total")));
        CATALOGO.put("cartera", new Def("Cartera pendiente de pago", "FINANCIERO", true, List.of(
                new Col("numero", "Factura", 'T'), new Col("fecha", "Fecha", 'T'), new Col("propietario", "Cliente", 'T'),
                new Col("telefono", "Teléfono", 'T'), new Col("estado", "Estado", 'T'), new Col("total", "Total", 'M'),
                new Col("pagado", "Pagado", 'M'), new Col("saldo", "Saldo", 'M')),
                List.of("total", "pagado", "saldo")));
        CATALOGO.put("existencias", new Def("Estado del inventario", "INVENTARIO", false, List.of(
                new Col("codigo", "Código", 'T'), new Col("nombre", "Nombre", 'T'), new Col("tipo", "Tipo", 'T'),
                new Col("invima", "Invima", 'T'), new Col("stock", "Stock", 'N'), new Col("stockMinimo", "Mínimo", 'N'),
                new Col("estado", "Estado", 'T'), new Col("proximoVence", "Próx. vencimiento", 'T'),
                new Col("precioCompra", "P. compra", 'M'), new Col("precioVenta", "P. venta", 'M'),
                new Col("valorInventario", "Valor inventario", 'M')),
                List.of("stock", "valorInventario")));
        CATALOGO.put("movimientos", new Def("Kárdex: movimientos de inventario", "INVENTARIO", true, List.of(
                new Col("fecha", "Fecha", 'T'), new Col("codigo", "Código", 'T'), new Col("nombre", "Medicamento/Producto", 'T'),
                new Col("lote", "Lote", 'T'), new Col("tipo", "Movimiento", 'T'), new Col("cantidad", "Cantidad", 'N'),
                new Col("saldo", "Saldo", 'N'), new Col("usuario", "Usuario", 'T'), new Col("observacion", "Observación", 'T')),
                List.of()));
        CATALOGO.put("por-vencer", new Def("Lotes vencidos y por vencer", "INVENTARIO", true, List.of(
                new Col("codigo", "Código", 'T'), new Col("nombre", "Nombre", 'T'), new Col("lote", "Lote", 'T'),
                new Col("vence", "Vence", 'T'), new Col("diasRestantes", "Días", 'N'), new Col("cantidad", "Unidades", 'N'),
                new Col("estado", "Estado", 'T')),
                List.of("cantidad")));
        CATALOGO.put("citas", new Def("Citas por veterinario y estado", "GESTION", true, List.of(
                new Col("medico", "Veterinario", 'T'), new Col("especialidad", "Especialidad", 'T'), new Col("estado", "Estado", 'T'),
                new Col("cantidad", "Citas", 'N')),
                List.of("cantidad")));
        CATALOGO.put("atenciones", new Def("Atenciones médicas realizadas", "GESTION", true, List.of(
                new Col("fecha", "Fecha", 'T'), new Col("medico", "Veterinario", 'T'), new Col("especialidad", "Especialidad", 'T'),
                new Col("mascota", "Mascota", 'T'), new Col("propietario", "Propietario", 'T'), new Col("diagnostico", "Diagnóstico", 'T'),
                new Col("estado", "Estado", 'T')),
                List.of()));
        CATALOGO.put("medicamentos-formulados", new Def("Medicamentos formulados vs dispensados", "GESTION", true, List.of(
                new Col("codigo", "Código", 'T'), new Col("nombre", "Medicamento", 'T'), new Col("formulas", "Fórmulas", 'N'),
                new Col("prescrito", "Prescrito", 'N'), new Col("dispensado", "Dispensado", 'N'), new Col("pendiente", "Pendiente", 'N')),
                List.of("formulas", "prescrito", "dispensado", "pendiente")));
        CATALOGO.put("despachos", new Def("Despachos realizados", "GESTION", true, List.of(
                new Col("comprobante", "Comprobante", 'T'), new Col("fecha", "Fecha", 'T'), new Col("formula", "Fórmula", 'T'),
                new Col("mascota", "Mascota", 'T'), new Col("estado", "Estado", 'T'), new Col("unidades", "Unidades", 'N'),
                new Col("usuario", "Despachó", 'T')),
                List.of("unidades")));
    }

    private static final Set<String> ESTADOS_STOCK = Set.of("AGOTADO", "STOCK_BAJO", "POR_VENCER", "VENCIDO", "DISPONIBLE");

    private final Db db;
    private final CurrentUser current;
    private final int diasPorVencer;

    public ReportesService(Db db, CurrentUser current,
                           @org.springframework.beans.factory.annotation.Value("${app.inventario.dias-por-vencer:60}") int diasPorVencer) {
        this.db = db;
        this.current = current;
        this.diasPorVencer = diasPorVencer;
    }

    /** Resultado calculado de un reporte. */
    public record Resultado(String clave, String titulo, String grupo, String filtros, List<Col> cols,
                            List<Map<String, Object>> filas, Map<String, Object> totales) {
    }

    public List<Map<String, Object>> catalogo() {
        List<Map<String, Object>> out = new ArrayList<>();
        CATALOGO.forEach((k, d) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("clave", k);
            m.put("titulo", d.titulo());
            m.put("grupo", d.grupo());
            m.put("usaFechas", d.usaFechas());
            out.add(m);
        });
        return out;
    }

    /** Vista previa en JSON. */
    public Map<String, Object> vistaPrevia(String clave, LocalDate desde, LocalDate hasta, Map<String, String> filtros) {
        Resultado r = calcular(clave, desde, hasta, filtros);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("clave", r.clave());
        m.put("titulo", r.titulo());
        m.put("grupo", r.grupo());
        m.put("filtros", r.filtros());
        m.put("generado", PdfKit.now());
        m.put("usuario", current.get().username());
        List<Map<String, String>> cols = new ArrayList<>();
        for (Col c : r.cols()) {
            cols.add(Map.of("key", c.key(), "header", c.header(), "type", String.valueOf(c.type())));
        }
        m.put("columnas", cols);
        m.put("filas", r.filas());
        m.put("totales", r.totales());
        m.put("cantidadFilas", r.filas().size());
        return m;
    }

    // ------------------------------------------------------------------ cálculo

    public Resultado calcular(String clave, LocalDate desde, LocalDate hasta, Map<String, String> f) {
        Def def = CATALOGO.get(clave);
        if (def == null) {
            throw new BadRequestException("Reporte desconocido: " + clave, HttpStatus.NOT_FOUND);
        }
        if (def.usaFechas()) {
            if (desde == null || hasta == null) {
                throw new BadRequestException("Indique el rango de fechas (desde y hasta).");
            }
            if (hasta.isBefore(desde)) {
                throw new BadRequestException("La fecha final no puede ser anterior a la inicial.");
            }
        }
        List<Object> a = new ArrayList<>();
        List<Map<String, Object>> filas;
        StringBuilder desc = new StringBuilder();
        if (def.usaFechas()) {
            desc.append("Del ").append(desde).append(" al ").append(hasta);
        }
        switch (clave) {
            case "facturacion" -> {
                StringBuilder sql = new StringBuilder("""
                        SELECT f.numero, DATE_FORMAT(f.fecha_generacion,'%Y-%m-%d %H:%i') AS fecha,
                               CONCAT(cl.nombres,' ',cl.apellidos) AS propietario, ma.nombre_mascota AS mascota,
                               CONCAT(me.nombres,' ',me.apellidos) AS medico, COALESCE(es.nombre,'') AS especialidad,
                               f.estado, f.subtotal, f.total_iva AS iva, f.descuento, f.total, f.total_pagado AS pagado,
                               ROUND(f.total - f.total_pagado,2) AS saldo
                          FROM factura f JOIN cliente cl ON cl.cliente_id = f.cliente_id
                          JOIN mascota ma ON ma.mascota_id = f.mascota_id JOIN medico me ON me.id = f.medico_id
                          LEFT JOIN especializacion es ON es.id = f.especializacion_id
                         WHERE f.fecha_generacion >= ? AND f.fecha_generacion < ?
                        """);
                a.add(desde.atStartOfDay());
                a.add(hasta.plusDays(1).atStartOfDay());
                filtroEstado(f, "estado", "f.estado", sql, a, desc, Set.of("PENDIENTE_PAGO", "PAGO_PARCIAL", "PAGADA", "ANULADA"));
                filtroId(f, "medicoId", "f.medico_id", sql, a, desc, "Veterinario");
                filtroId(f, "especializacionId", "f.especializacion_id", sql, a, desc, "Especialidad");
                sql.append(" ORDER BY f.id");
                filas = db.list(sql.toString(), a.toArray());
                // Las facturas anuladas se listan, pero no suman en los totales
                filas.forEach(r -> {
                    if ("ANULADA".equals(r.get("estado"))) {
                        r.put("_excluir", true);
                    }
                });
            }
            case "ingresos" -> {
                StringBuilder sql = new StringBuilder("""
                        SELECT DATE_FORMAT(p.fecha_pago,'%Y-%m-%d %H:%i') AS fecha, fa.numero AS factura,
                               CONCAT(cl.nombres,' ',cl.apellidos) AS propietario, p.metodo, COALESCE(p.referencia,'') AS referencia,
                               u.username AS usuario, p.monto
                          FROM pago p JOIN factura fa ON fa.id = p.factura_id JOIN cliente cl ON cl.cliente_id = fa.cliente_id
                          JOIN usuario u ON u.id = p.usuario_id
                         WHERE fa.estado <> 'ANULADA' AND p.fecha_pago >= ? AND p.fecha_pago < ?
                        """);
                a.add(desde.atStartOfDay());
                a.add(hasta.plusDays(1).atStartOfDay());
                filtroEstado(f, "metodo", "p.metodo", sql, a, desc, Set.of("EFECTIVO", "TARJETA", "TRANSFERENCIA", "OTRO"));
                sql.append(" ORDER BY p.id");
                filas = db.list(sql.toString(), a.toArray());
            }
            case "ingresos-por-servicio" -> {
                StringBuilder sql = new StringBuilder("""
                        SELECT d.tipo, d.descripcion, SUM(d.cantidad) AS cantidad, SUM(d.subtotal) AS subtotal,
                               SUM(d.valor_iva) AS iva, SUM(d.total) AS total
                          FROM factura_detalle d JOIN factura f ON f.id = d.factura_id
                         WHERE f.estado <> 'ANULADA' AND f.fecha_generacion >= ? AND f.fecha_generacion < ?
                        """);
                a.add(desde.atStartOfDay());
                a.add(hasta.plusDays(1).atStartOfDay());
                filtroEstado(f, "tipo", "d.tipo", sql, a, desc, Set.of("CONSULTA", "SERVICIO", "MEDICAMENTO"));
                sql.append(" GROUP BY d.tipo, d.descripcion ORDER BY total DESC");
                filas = db.list(sql.toString(), a.toArray());
            }
            case "cartera" -> {
                filas = db.list("""
                        SELECT f.numero, DATE_FORMAT(f.fecha_generacion,'%Y-%m-%d') AS fecha,
                               CONCAT(cl.nombres,' ',cl.apellidos) AS propietario, COALESCE(cl.telefono,'') AS telefono, f.estado,
                               f.total, f.total_pagado AS pagado, ROUND(f.total - f.total_pagado,2) AS saldo
                          FROM factura f JOIN cliente cl ON cl.cliente_id = f.cliente_id
                         WHERE f.estado IN ('PENDIENTE_PAGO','PAGO_PARCIAL') AND f.fecha_generacion >= ? AND f.fecha_generacion < ?
                         ORDER BY f.fecha_generacion
                        """, desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
            }
            case "existencias" -> {
                String estado = f.get("estado") == null ? null : f.get("estado").trim().toUpperCase(Locale.ROOT);
                if (estado != null && !estado.isEmpty() && !ESTADOS_STOCK.contains(estado)) {
                    throw new BadRequestException("Estado de inventario inválido: " + estado);
                }
                StringBuilder sql = new StringBuilder("""
                        SELECT x.* FROM (
                          SELECT m.codigo, m.nombre, m.tipo, COALESCE(m.registro_invima,'') AS invima, m.stock_actual AS stock,
                                 m.stock_minimo, m.precio_compra, m.precio_venta,
                                 ROUND(m.stock_actual * m.precio_compra,2) AS valor_inventario,
                                 (SELECT DATE_FORMAT(MIN(l.fecha_vencimiento),'%Y-%m-%d') FROM medicamento_lote l
                                   WHERE l.medicamento_id = m.id AND l.cantidad_disponible > 0 AND l.fecha_vencimiento IS NOT NULL) AS proximo_vence,
                                 CASE WHEN EXISTS (SELECT 1 FROM medicamento_lote l WHERE l.medicamento_id = m.id AND l.cantidad_disponible > 0
                                                    AND l.fecha_vencimiento < CURDATE()) THEN 'VENCIDO'
                                      WHEN m.stock_actual = 0 THEN 'AGOTADO'
                                      WHEN m.stock_actual <= m.stock_minimo THEN 'STOCK_BAJO'
                                      WHEN EXISTS (SELECT 1 FROM medicamento_lote l WHERE l.medicamento_id = m.id AND l.cantidad_disponible > 0
                                                    AND l.fecha_vencimiento <= DATE_ADD(CURDATE(), INTERVAL ? DAY)) THEN 'POR_VENCER'
                                      ELSE 'DISPONIBLE' END AS estado
                            FROM medicamento m WHERE m.activo = 1
                        ) x WHERE 1=1
                        """);
                a.add(diasPorVencer);
                if (estado != null && !estado.isEmpty()) {
                    sql.append(" AND x.estado = ?");
                    a.add(estado);
                    desc.append(" Estado: ").append(estado);
                }
                String tipo = f.get("tipo");
                if (tipo != null && !tipo.isBlank()) {
                    sql.append(" AND x.tipo = ?");
                    a.add(tipo.trim().toUpperCase(Locale.ROOT));
                    desc.append(" Tipo: ").append(tipo.trim().toUpperCase(Locale.ROOT));
                }
                sql.append(" ORDER BY x.nombre");
                filas = db.list(sql.toString(), a.toArray());
                filas.forEach(r -> {
                    r.put("stockMinimo", r.get("stockMinimo"));
                    r.put("proximoVence", r.get("proximoVence"));
                });
                if (desc.length() == 0) {
                    desc.append("Corte a ").append(LocalDate.now());
                }
            }
            case "movimientos" -> {
                StringBuilder sql = new StringBuilder("""
                        SELECT DATE_FORMAT(mv.fecha,'%Y-%m-%d %H:%i') AS fecha, m.codigo, m.nombre, COALESCE(l.numero_lote,'') AS lote,
                               mv.tipo, mv.cantidad, mv.saldo_posterior AS saldo, u.username AS usuario,
                               COALESCE(mv.observacion,'') AS observacion
                          FROM inventario_movimiento mv JOIN medicamento m ON m.id = mv.medicamento_id
                          LEFT JOIN medicamento_lote l ON l.id = mv.lote_id JOIN usuario u ON u.id = mv.usuario_id
                         WHERE mv.fecha >= ? AND mv.fecha < ?
                        """);
                a.add(desde.atStartOfDay());
                a.add(hasta.plusDays(1).atStartOfDay());
                filtroEstado(f, "tipo", "mv.tipo", sql, a, desc, Set.of("ENTRADA", "SALIDA", "AJUSTE", "REVERSION"));
                filtroId(f, "medicamentoId", "mv.medicamento_id", sql, a, desc, "Medicamento");
                sql.append(" ORDER BY mv.id");
                filas = db.list(sql.toString(), a.toArray());
            }
            case "por-vencer" -> {
                filas = db.list("""
                        SELECT m.codigo, m.nombre, l.numero_lote AS lote, DATE_FORMAT(l.fecha_vencimiento,'%Y-%m-%d') AS vence,
                               DATEDIFF(l.fecha_vencimiento, CURDATE()) AS dias_restantes, l.cantidad_disponible AS cantidad,
                               CASE WHEN l.fecha_vencimiento < CURDATE() THEN 'VENCIDO' ELSE 'POR_VENCER' END AS estado
                          FROM medicamento_lote l JOIN medicamento m ON m.id = l.medicamento_id
                         WHERE l.cantidad_disponible > 0 AND l.fecha_vencimiento IS NOT NULL
                           AND l.fecha_vencimiento BETWEEN ? AND ?
                         ORDER BY l.fecha_vencimiento
                        """, desde, hasta);
            }
            case "citas" -> {
                StringBuilder sql = new StringBuilder("""
                        SELECT CONCAT(me.nombres,' ',me.apellidos) AS medico, es.nombre AS especialidad, e.nombre AS estado,
                               COUNT(*) AS cantidad
                          FROM cita c JOIN medico me ON me.id = c.medico_id JOIN especializacion es ON es.id = me.especializacion_id
                          JOIN estado_cita e ON e.codigo = c.estado
                         WHERE c.fecha_hora >= ? AND c.fecha_hora < ?
                        """);
                a.add(desde.atStartOfDay());
                a.add(hasta.plusDays(1).atStartOfDay());
                filtroId(f, "medicoId", "c.medico_id", sql, a, desc, "Veterinario");
                filtroId(f, "especializacionId", "me.especializacion_id", sql, a, desc, "Especialidad");
                sql.append(" GROUP BY me.id, me.nombres, me.apellidos, es.nombre, e.nombre ORDER BY medico, estado");
                filas = db.list(sql.toString(), a.toArray());
            }
            case "atenciones" -> {
                StringBuilder sql = new StringBuilder("""
                        SELECT DATE_FORMAT(a.fecha_inicio,'%Y-%m-%d %H:%i') AS fecha, CONCAT(me.nombres,' ',me.apellidos) AS medico,
                               es.nombre AS especialidad, ma.nombre_mascota AS mascota,
                               CONCAT(cl.nombres,' ',cl.apellidos) AS propietario, COALESCE(a.diagnostico,'') AS diagnostico, a.estado
                          FROM atencion_medica a JOIN medico me ON me.id = a.medico_id
                          JOIN especializacion es ON es.id = me.especializacion_id JOIN mascota ma ON ma.mascota_id = a.mascota_id
                          JOIN cita c ON c.id = a.cita_id JOIN cliente cl ON cl.cliente_id = c.cliente_id
                         WHERE a.fecha_inicio >= ? AND a.fecha_inicio < ?
                        """);
                a.add(desde.atStartOfDay());
                a.add(hasta.plusDays(1).atStartOfDay());
                filtroId(f, "medicoId", "a.medico_id", sql, a, desc, "Veterinario");
                filtroId(f, "especializacionId", "me.especializacion_id", sql, a, desc, "Especialidad");
                sql.append(" ORDER BY a.fecha_inicio");
                filas = db.list(sql.toString(), a.toArray());
            }
            case "medicamentos-formulados" -> {
                filas = db.list("""
                        SELECT m.codigo, m.nombre, COUNT(DISTINCT fm.id) AS formulas, SUM(d.cantidad_prescrita) AS prescrito,
                               SUM(d.cantidad_dispensada) AS dispensado,
                               SUM(d.cantidad_prescrita - d.cantidad_dispensada) AS pendiente
                          FROM formula_medica fm JOIN formula_medica_detalle d ON d.formula_id = fm.id
                          JOIN medicamento m ON m.id = d.medicamento_id
                         WHERE fm.estado = 'CONFIRMADA' AND fm.fecha_creacion_registro >= ? AND fm.fecha_creacion_registro < ?
                         GROUP BY m.id, m.codigo, m.nombre ORDER BY prescrito DESC
                        """, desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
            }
            case "despachos" -> {
                StringBuilder sql = new StringBuilder("""
                        SELECT COALESCE(d.numero_comprobante,'') AS comprobante, DATE_FORMAT(d.fecha_creacion,'%Y-%m-%d %H:%i') AS fecha,
                               CAST(d.formula_id AS CHAR) AS formula, ma.nombre_mascota AS mascota, d.estado,
                               (SELECT COALESCE(SUM(x.cantidad_entregada),0) FROM despacho_detalle x WHERE x.despacho_id = d.id) AS unidades,
                               u.username AS usuario
                          FROM despacho d JOIN formula_medica fm ON fm.id = d.formula_id JOIN mascota ma ON ma.mascota_id = fm.mascota_id
                          JOIN usuario u ON u.id = d.usuario_id
                         WHERE d.fecha_creacion >= ? AND d.fecha_creacion < ?
                        """);
                a.add(desde.atStartOfDay());
                a.add(hasta.plusDays(1).atStartOfDay());
                filtroEstado(f, "estado", "d.estado", sql, a, desc, Set.of("DESPACHADO", "DESPACHO_PARCIAL", "CANCELADO", "EN_PREPARACION"));
                sql.append(" ORDER BY d.id");
                filas = db.list(sql.toString(), a.toArray());
            }
            default -> throw new BadRequestException("Reporte desconocido: " + clave, HttpStatus.NOT_FOUND);
        }

        Map<String, Object> totales = new LinkedHashMap<>();
        for (String k : def.sumar()) {
            BigDecimal s = BigDecimal.ZERO;
            for (Map<String, Object> r : filas) {
                if (Boolean.TRUE.equals(r.get("_excluir"))) {
                    continue;
                }
                Object v = r.get(k);
                if (v != null) {
                    s = s.add(In.toDec(v));
                }
            }
            totales.put(k, s);
        }
        filas.forEach(r -> r.remove("_excluir"));
        if (clave.equals("facturacion")) {
            desc.append(" (totales sin facturas anuladas)");
        }
        return new Resultado(clave, def.titulo(), def.grupo(), desc.toString().trim(), def.cols(), filas, totales);
    }

    private void filtroEstado(Map<String, String> f, String param, String col, StringBuilder sql, List<Object> args,
                              StringBuilder desc, Set<String> validos) {
        String v = f.get(param);
        if (v == null || v.isBlank()) {
            return;
        }
        v = v.trim().toUpperCase(Locale.ROOT);
        if (!validos.contains(v)) {
            throw new BadRequestException("Valor inválido para '" + param + "': " + v + ". Use: " + String.join(", ", validos) + ".");
        }
        sql.append(" AND ").append(col).append(" = ?");
        args.add(v);
        desc.append(" | ").append(param).append(": ").append(v);
    }

    private void filtroId(Map<String, String> f, String param, String col, StringBuilder sql, List<Object> args,
                          StringBuilder desc, String etiqueta) {
        String v = f.get(param);
        if (v == null || v.isBlank()) {
            return;
        }
        long id;
        try {
            id = Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("El filtro '" + param + "' debe ser numérico.");
        }
        sql.append(" AND ").append(col).append(" = ?");
        args.add(id);
        desc.append(" | ").append(etiqueta).append(" #").append(id);
    }

    // ------------------------------------------------------------------ exportación

    public byte[] excel(String clave, LocalDate desde, LocalDate hasta, Map<String, String> filtros) {
        Resultado r = calcular(clave, desde, hasta, filtros);
        List<String> headers = r.cols().stream().map(Col::header).toList();
        List<List<Object>> rows = new ArrayList<>();
        for (Map<String, Object> fila : r.filas()) {
            List<Object> row = new ArrayList<>();
            for (Col c : r.cols()) {
                Object v = fila.get(c.key());
                row.add(c.type() == 'T' ? (v == null ? "" : String.valueOf(v)) : (v == null ? null : In.toDec(v)));
            }
            rows.add(row);
        }
        List<Object> total = null;
        if (!r.totales().isEmpty()) {
            total = new ArrayList<>();
            boolean etiqueta = false;
            for (Col c : r.cols()) {
                if (r.totales().containsKey(c.key())) {
                    total.add(r.totales().get(c.key()));
                } else if (!etiqueta) {
                    total.add("TOTAL");
                    etiqueta = true;
                } else {
                    total.add(null);
                }
            }
        }
        String info = "Generado: " + PdfKit.now() + " | Usuario: " + current.get().username()
                + (r.filtros().isEmpty() ? "" : " | " + r.filtros());
        return XlsxKit.sheet(r.titulo(), r.titulo(), info, headers, rows, total);
    }

    public byte[] pdf(String clave, LocalDate desde, LocalDate hasta, Map<String, String> filtros) {
        Resultado r = calcular(clave, desde, hasta, filtros);
        Map<String, Object> clinica = db.one("SELECT * FROM clinica_config WHERE id = 1");
        boolean ancho = r.cols().size() > 6;
        return PdfKit.render(ancho, doc -> {
            PdfKit.header(doc, clinica, r.titulo().toUpperCase(Locale.ROOT));
            PdfKit.line(doc, "Generado", PdfKit.now() + "  por " + current.get().username());
            if (!r.filtros().isEmpty()) {
                PdfKit.line(doc, "Filtros", r.filtros());
            }
            PdfKit.line(doc, "Registros", String.valueOf(r.filas().size()));
            float[] w = new float[r.cols().size()];
            String[] heads = new String[w.length];
            for (int i = 0; i < w.length; i++) {
                Col c = r.cols().get(i);
                w[i] = c.type() == 'T' ? 2f : 1.3f;
                heads[i] = c.header();
            }
            PdfPTable t = PdfKit.table(w, heads);
            for (Map<String, Object> fila : r.filas()) {
                for (Col c : r.cols()) {
                    Object v = fila.get(c.key());
                    String txt = v == null ? "" : c.type() == 'M' ? PdfKit.money(v) : String.valueOf(v);
                    PdfKit.cell(t, txt, c.type() != 'T');
                }
            }
            if (!r.totales().isEmpty()) {
                boolean etiqueta = false;
                for (Col c : r.cols()) {
                    if (r.totales().containsKey(c.key())) {
                        Object v = r.totales().get(c.key());
                        PdfKit.cellBold(t, c.type() == 'M' ? PdfKit.money(v) : String.valueOf(v), true);
                    } else if (!etiqueta) {
                        PdfKit.cellBold(t, "TOTAL", false);
                        etiqueta = true;
                    } else {
                        PdfKit.cellBold(t, "", false);
                    }
                }
            }
            doc.add(t);
        });
    }
}
