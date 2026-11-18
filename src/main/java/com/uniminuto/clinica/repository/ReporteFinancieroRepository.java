package com.uniminuto.clinica.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Resume facturas por fecha de emisión y calcula el recaudo neto por las fechas de pago y
 * devolución. Aplica los filtros a partir de la especialidad guardada en la factura.
 */
@Repository
@RequiredArgsConstructor
public class ReporteFinancieroRepository {

    private final JdbcTemplate jdbc;

    /**
     * Calcula el resumen de facturación, los estados, el IVA y los descuentos, y distingue pagos
     * de devoluciones.
     */
    public List<List<Object>> consultar(LocalDateTime desde, LocalDateTime hasta, String formato,
                                        Long medico, Long especialidad) {
        Object[] filtros = {desde, hasta, medico, medico, especialidad, especialidad};
        var filas = new ArrayList<List<Object>>();
        filas.addAll(jdbc.query("""
                WITH facturas_periodo AS (
                    SELECT DATE_FORMAT(f.fecha_emision, '%s') AS grupo, f.estado, f.total, f.iva, f.descuento
                    FROM factura f JOIN medico m ON m.id = f.medico_id
                    WHERE f.tipo_documento = 'FACTURA' AND f.fecha_emision >= ? AND f.fecha_emision < ?
                        AND (? IS NULL OR m.id = ?) AND (? IS NULL OR COALESCE(f.especializacion_emision_id, m.especializacion_id) = ?)
                )
                SELECT COALESCE(grupo, 'TOTAL'), COUNT(*),
                    COALESCE(SUM(estado = 'PAGADA'),0), COALESCE(SUM(estado = 'PENDIENTE'),0),
                    COALESCE(SUM(estado = 'PAGO_PARCIAL'),0), COALESCE(SUM(estado = 'ANULADA'),0),
                    COALESCE(SUM(CASE WHEN estado <> 'ANULADA' THEN total END),0),
                    COALESCE(SUM(CASE WHEN estado <> 'ANULADA' THEN iva END),0),
                    COALESCE(SUM(CASE WHEN estado <> 'ANULADA' THEN descuento END),0),
                    COALESCE(SUM(estado = 'NO_REQUIERE_PAGO'),0)
                FROM facturas_periodo GROUP BY grupo WITH ROLLUP
                """.formatted(formato), (rs, i) -> List.<Object>of("Facturación", rs.getString(1),
                        rs.getLong(2), rs.getLong(3), rs.getLong(4), rs.getLong(5), rs.getLong(6),
                        rs.getBigDecimal(7), rs.getBigDecimal(8), rs.getBigDecimal(9), rs.getLong(10)), filtros));

        filas.addAll(jdbc.query("""
                WITH pagos_periodo AS (
                    SELECT DATE_FORMAT(p.fecha_pago, '%s') AS grupo, p.monto
                    FROM pago p JOIN factura f ON f.id = p.factura_id JOIN medico m ON m.id = f.medico_id
                    WHERE f.tipo_documento = 'FACTURA' AND p.fecha_pago >= ? AND p.fecha_pago < ?
                        AND (? IS NULL OR m.id = ?) AND (? IS NULL OR COALESCE(f.especializacion_emision_id, m.especializacion_id) = ?)
                )
                SELECT COALESCE(grupo, 'TOTAL'), COALESCE(SUM(monto),0)
                FROM pagos_periodo GROUP BY grupo WITH ROLLUP
                """.formatted(formato), (rs, i) -> List.<Object>of("Recaudo", rs.getString(1),
                        "", "", "", "", "", rs.getBigDecimal(2), "", ""), filtros));

        filas.addAll(jdbc.query("""
                WITH devoluciones_periodo AS (
                    SELECT DATE_FORMAT(d.fecha_devolucion, '%s') AS grupo, p.monto
                    FROM devolucion_pago d JOIN pago p ON p.id = d.pago_id
                    JOIN factura f ON f.id = p.factura_id JOIN medico m ON m.id = f.medico_id
                    WHERE f.tipo_documento = 'FACTURA' AND d.fecha_devolucion >= ? AND d.fecha_devolucion < ?
                        AND (? IS NULL OR m.id = ?) AND (? IS NULL OR COALESCE(f.especializacion_emision_id, m.especializacion_id) = ?)
                )
                SELECT COALESCE(grupo, 'TOTAL'), COALESCE(SUM(monto),0)
                FROM devoluciones_periodo GROUP BY grupo WITH ROLLUP
                """.formatted(formato), (rs, i) -> List.<Object>of("Devoluciones", rs.getString(1),
                        "", "", "", "", "", rs.getBigDecimal(2).negate(), "", ""), filtros));

        filas.addAll(jdbc.query("""
                WITH movimientos AS (
                    SELECT p.fecha_pago AS fecha, p.monto AS importe, p.factura_id
                    FROM pago p
                    WHERE p.fecha_pago >= ? AND p.fecha_pago < ?
                    UNION ALL
                    SELECT d.fecha_devolucion, -p.monto, p.factura_id
                    FROM devolucion_pago d JOIN pago p ON p.id = d.pago_id
                    WHERE d.fecha_devolucion >= ? AND d.fecha_devolucion < ?
                ), movimientos_periodo AS (
                    SELECT DATE_FORMAT(mv.fecha, '%s') AS grupo, mv.importe
                    FROM movimientos mv JOIN factura f ON f.id = mv.factura_id JOIN medico m ON m.id = f.medico_id
                    WHERE f.tipo_documento = 'FACTURA' AND (? IS NULL OR m.id = ?)
                        AND (? IS NULL OR COALESCE(f.especializacion_emision_id, m.especializacion_id) = ?)
                )
                SELECT COALESCE(grupo, 'TOTAL'), COALESCE(SUM(importe), 0)
                FROM movimientos_periodo GROUP BY grupo WITH ROLLUP
                """.formatted(formato), (rs, i) -> List.<Object>of("Recaudo neto", rs.getString(1),
                        "", "", "", "", "", rs.getBigDecimal(2), "", ""),
                desde, hasta, desde, hasta, medico, medico, especialidad, especialidad));

        filas.addAll(recaudoPorConcepto(desde, hasta, formato, medico, especialidad));

        filas.addAll(jdbc.query("""
                SELECT e.nombre, CONCAT(m.nombres, ' ', m.apellidos), d.tipo, SUM(d.subtotal)
                FROM factura f JOIN factura_detalle d ON d.factura_id = f.id
                JOIN medico m ON m.id = f.medico_id JOIN especializacion e ON e.id = COALESCE(f.especializacion_emision_id, m.especializacion_id)
                WHERE f.tipo_documento = 'FACTURA' AND f.estado <> 'ANULADA'
                    AND f.fecha_emision >= ? AND f.fecha_emision < ?
                    AND (? IS NULL OR m.id = ?) AND (? IS NULL OR COALESCE(f.especializacion_emision_id, m.especializacion_id) = ?)
                GROUP BY e.id, e.nombre, m.id, m.nombres, m.apellidos, d.tipo
                ORDER BY e.nombre, m.nombres, m.apellidos, d.tipo
                """, (rs, i) -> List.<Object>of("Subtotal " + rs.getString(3),
                        rs.getString(1) + " / " + rs.getString(2), "", "", "", "", "", rs.getBigDecimal(4), "", ""),
                filtros));

        return filas;
    }

    /** Distribuye proporcionalmente abonos, IVA y descuentos entre servicios y medicamentos. */
    private List<List<Object>> recaudoPorConcepto(LocalDateTime desde, LocalDateTime hasta, String formato,
                                                  Long medico, Long especialidad) {
        // La diferencia entre acumulados redondeados conserva los centavos entre abonos.
        // La devolución usa el pago original y cambia el signo en la fecha de devolución.
        return jdbc.query("""
                WITH eventos AS (
                    SELECT p.id AS pago_id, p.factura_id, p.fecha_pago AS fecha, 1 AS signo
                    FROM pago p WHERE p.fecha_pago >= ? AND p.fecha_pago < ?
                    UNION ALL
                    SELECT p.id, p.factura_id, d.fecha_devolucion, -1
                    FROM devolucion_pago d JOIN pago p ON p.id = d.pago_id
                    WHERE d.fecha_devolucion >= ? AND d.fecha_devolucion < ?
                ), facturas_del_reporte AS (
                    SELECT DISTINCT f.id, f.total, f.descuento
                    FROM eventos ev JOIN factura f ON f.id = ev.factura_id JOIN medico m ON m.id = f.medico_id
                    WHERE f.tipo_documento = 'FACTURA' AND f.total > 0
                        AND (? IS NULL OR m.id = ?)
                        AND (? IS NULL OR COALESCE(f.especializacion_emision_id, m.especializacion_id) = ?)
                ), importes AS (
                    SELECT f.id, f.total, f.descuento,
                        SUM(d.subtotal + COALESCE(i.importe, 0)) AS bruto,
                        SUM(CASE WHEN d.tipo = 'SERVICIO' THEN d.subtotal + COALESCE(i.importe, 0) ELSE 0 END)
                            AS bruto_servicio,
                        SUM(CASE WHEN d.tipo = 'SERVICIO' THEN COALESCE(i.importe, 0) ELSE 0 END) AS iva_servicio,
                        SUM(CASE WHEN d.tipo = 'MEDICAMENTO' THEN COALESCE(i.importe, 0) ELSE 0 END) AS iva_medicamento
                    FROM facturas_del_reporte f JOIN factura_detalle d ON d.factura_id = f.id
                    LEFT JOIN factura_detalle_iva i ON i.detalle_id = d.id
                    GROUP BY f.id, f.total, f.descuento
                    HAVING bruto > 0
                ), costos AS (
                    SELECT i.*, ROUND(i.descuento * i.bruto_servicio / i.bruto, 2) AS descuento_servicio
                    FROM importes i
                ), pagos_acumulados AS (
                    SELECT p.id, p.factura_id, p.monto,
                        SUM(p.monto) OVER (PARTITION BY p.factura_id ORDER BY p.id ROWS UNBOUNDED PRECEDING) AS acumulado
                    FROM pago p JOIN costos i ON i.id = p.factura_id
                ), bases AS (
                    SELECT DATE_FORMAT(ev.fecha, '%s') AS grupo, ev.signo, p.monto, p.acumulado,
                        p.acumulado - p.monto AS previo, i.total,
                        i.bruto_servicio - i.descuento_servicio AS neto_servicio,
                        COALESCE(ROUND(i.iva_servicio * (i.bruto_servicio - i.descuento_servicio)
                            / NULLIF(i.bruto_servicio, 0), 2), 0) AS iva_servicio,
                        COALESCE(ROUND(i.iva_medicamento * (i.total - i.bruto_servicio + i.descuento_servicio)
                            / NULLIF(i.bruto - i.bruto_servicio, 0), 2), 0) AS iva_medicamento,
                        i.descuento_servicio, i.descuento - i.descuento_servicio AS descuento_medicamento
                    FROM eventos ev JOIN pagos_acumulados p ON p.id = ev.pago_id JOIN costos i ON i.id = p.factura_id
                ), reparto AS (
                    SELECT grupo, signo, monto,
                        ROUND(acumulado * neto_servicio / total, 2) - ROUND(previo * neto_servicio / total, 2) AS servicio,
                        ROUND(acumulado * iva_servicio / total, 2) - ROUND(previo * iva_servicio / total, 2) AS iva_servicio,
                        ROUND(acumulado * iva_medicamento / total, 2) - ROUND(previo * iva_medicamento / total, 2)
                            AS iva_medicamento,
                        ROUND(acumulado * descuento_servicio / total, 2) - ROUND(previo * descuento_servicio / total, 2)
                            AS descuento_servicio,
                        ROUND(acumulado * descuento_medicamento / total, 2) - ROUND(previo * descuento_medicamento / total, 2)
                            AS descuento_medicamento
                    FROM bases
                ), conceptos AS (
                    SELECT 'Recaudo neto de servicios' AS concepto, grupo, signo * servicio AS importe,
                        signo * iva_servicio AS iva, signo * descuento_servicio AS descuento
                    FROM reparto
                    UNION ALL
                    SELECT 'Recaudo neto de medicamentos', grupo, signo * (monto - servicio),
                        signo * iva_medicamento, signo * descuento_medicamento
                    FROM reparto
                )
                SELECT concepto, COALESCE(grupo, 'TOTAL'), SUM(importe), SUM(iva), SUM(descuento)
                FROM conceptos
                GROUP BY concepto, grupo WITH ROLLUP
                HAVING GROUPING(concepto) = 0
                """.formatted(formato), (rs, i) -> List.<Object>of(rs.getString(1), rs.getString(2),
                        "", "", "", "", "", rs.getBigDecimal(3), rs.getBigDecimal(4), rs.getBigDecimal(5), ""),
                desde, hasta, desde, hasta, medico, medico, especialidad, especialidad);
    }
}
