package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Consulta y guarda las prefacturas, facturas, líneas, impuestos y pagos. Recupera los productos
 * efectivamente entregados para el cobro definitivo.
 */
@Repository
@RequiredArgsConstructor
public class FacturaRepository {

    private final JdbcTemplate jdbc;

    public List<Producto> medicamentosPrescritos(long citaId) {
        return jdbc.query("""
                SELECT m.id, m.nombre, d.cantidad_prescrita, m.precio_venta, COALESCE(i.iva_porcentaje,0)
                FROM formula_medica f JOIN formula_detalle d ON d.formula_id = f.id
                JOIN medicamento m ON m.id = d.medicamento_id
                LEFT JOIN impuesto_medicamento i ON i.medicamento_id = m.id
                WHERE f.cita_id = ? AND f.activo = 1 AND f.estado IN ('VIGENTE','COMPLETA')
                ORDER BY f.id, d.id
                """, (rs, index) -> new Producto(rs.getLong(1), rs.getString(2), rs.getInt(3),
                rs.getBigDecimal(4), null, rs.getBigDecimal(5)), citaId);
    }

    public List<Map<String, Object>> pendientes(long citaId) {
        return jdbc.queryForList("""
                SELECT fd.id FROM formula_detalle fd JOIN formula_medica f ON f.id = fd.formula_id
                WHERE f.cita_id = ? AND f.activo = 1 AND f.estado = 'VIGENTE'
                  AND fd.cantidad_prescrita > COALESCE((
                    SELECT SUM(dd.cantidad_entregada) FROM despacho_detalle dd JOIN despacho d ON d.id = dd.despacho_id
                    WHERE dd.formula_detalle_id = fd.id AND d.estado <> 'CANCELADO'), 0)
                """, citaId);
    }

    /** Lee los precios y el IVA guardados en la prefactura. */
    public List<Servicio> serviciosPrefacturados(long citaId) {
        return jdbc.query("""
                SELECT d.tarifa_id, d.descripcion, d.precio_unitario, COALESCE(i.porcentaje,0)
                FROM factura f JOIN factura_detalle d ON d.factura_id = f.id AND d.tipo = 'SERVICIO'
                LEFT JOIN factura_detalle_iva i ON i.detalle_id = d.id
                WHERE f.cita_id = ? AND f.tipo_documento = 'PREFACTURA' AND f.estado <> 'ANULADA'
                ORDER BY d.id
                """, (rs, index) -> new Servicio(rs.getLong(1), rs.getString(2), rs.getBigDecimal(3),
                rs.getBigDecimal(4)), citaId);
    }

    /** Lee las cantidades entregadas de despachos confirmados que siguen vigentes. */
    public List<Producto> entregas(long citaId) {
        return jdbc.query("""
                SELECT m.id, m.nombre, dd.cantidad_entregada, m.precio_venta, dd.id,
                       COALESCE(i.iva_porcentaje,0)
                FROM despacho_detalle dd JOIN despacho d ON d.id = dd.despacho_id
                JOIN formula_medica f ON d.formula_id = f.id JOIN medicamento m ON m.id = dd.medicamento_id
                LEFT JOIN impuesto_medicamento i ON i.medicamento_id = m.id
                WHERE f.cita_id = ? AND d.estado <> 'CANCELADO'
                  AND d.fecha_despacho IS NOT NULL AND dd.cantidad_entregada > 0
                ORDER BY dd.id
                """, (rs, index) -> new Producto(rs.getLong(1), rs.getString(2), rs.getInt(3),
                rs.getBigDecimal(4), rs.getLong(5), rs.getBigDecimal(6)), citaId);
    }

    public void bloquearDespachos(long citaId) {
        jdbc.queryForList("""
                SELECT d.id FROM despacho d JOIN formula_medica f ON f.id = d.formula_id
                WHERE f.cita_id = ? FOR UPDATE
                """, citaId);
    }

    public List<Map<String, Object>> historial(Long clienteId, Long mascotaId) {
        return jdbc.queryForList("""
                SELECT f.id, f.numero_factura AS numero, f.fecha_emision AS fecha, f.estado,
                       f.total, f.cliente_id AS clienteId, c.mascota_id AS mascotaId,
                       COALESCE((SELECT SUM(p.monto) FROM pago p LEFT JOIN devolucion_pago d ON d.pago_id = p.id
                                 WHERE p.factura_id = f.id AND d.pago_id IS NULL),0) AS pagado
                FROM factura f JOIN cita c ON c.id = f.cita_id
                WHERE f.tipo_documento = 'FACTURA' AND (? IS NULL OR f.cliente_id = ?)
                  AND (? IS NULL OR c.mascota_id = ?) ORDER BY f.fecha_emision DESC, f.id DESC
                """, clienteId, clienteId, mascotaId, mascotaId);
    }

    public List<Map<String, Object>> listarPagos(long facturaId) {
        return jdbc.queryForList("""
                SELECT p.id, p.monto, p.metodo_pago AS metodo, p.fecha_pago AS fecha, p.observacion,
                       d.referencia AS referenciaDevolucion, d.fecha_devolucion AS fechaDevolucion
                FROM pago p LEFT JOIN devolucion_pago d ON d.pago_id = p.id
                WHERE p.factura_id = ? ORDER BY p.fecha_pago, p.id
                """, facturaId);
    }

    public Map<String, Object> cabecera(long facturaId, Long citaId) {
        return jdbc.queryForList("""
                SELECT f.numero_factura, f.tipo_documento, f.fecha_emision, f.estado, f.subtotal, f.iva, f.descuento,
                       f.motivo_sin_pago, COALESCE(CONCAT_WS(' ', cl.tipo_documento, cl.numero_documento), '') AS documento_propietario,
                       CONCAT_WS(' / ', r.especie, r.nombre) AS identificacion_mascota,
                       COALESCE(m.registro_profesional, '') AS registro_profesional,
                       f.total, CONCAT(cl.nombres, ' ', cl.apellidos) AS propietario, COALESCE(a.nombre_mascota, '') AS mascota,
                       CONCAT(m.nombres, ' ', m.apellidos) AS medico, e.nombre AS especialidad
                FROM factura f JOIN cita c ON c.id = f.cita_id JOIN cliente cl ON cl.cliente_id = f.cliente_id
                JOIN mascota a ON a.mascota_id = c.mascota_id JOIN medico m ON m.id = f.medico_id
                JOIN especializacion e ON e.id = COALESCE(f.especializacion_emision_id, m.especializacion_id)
                LEFT JOIN raza r ON r.raza_id = a.raza_id
                WHERE f.id = ? AND (? IS NULL OR (f.cita_id = ? AND f.tipo_documento = 'PREFACTURA'))
                """, facturaId, citaId, citaId).stream().findFirst()
                .orElseThrow(() -> new BadRequestException("El documento no existe."));
    }

    public List<List<Object>> lineas(long facturaId) {
        return jdbc.query("""
                SELECT d.tipo, d.descripcion, d.cantidad, d.precio_unitario, d.subtotal, COALESCE(i.importe,0)
                FROM factura_detalle d LEFT JOIN factura_detalle_iva i ON i.detalle_id = d.id
                WHERE d.factura_id = ? ORDER BY d.id
                """, (rs, index) -> List.<Object>of(rs.getString(1), rs.getString(2), rs.getInt(3),
                rs.getBigDecimal(4), rs.getBigDecimal(5), rs.getBigDecimal(6)), facturaId);
    }

    public long insertarFactura(Map<String, ?> valores) {
        return new SimpleJdbcInsert(jdbc).withTableName("factura")
                .usingColumns(valores.keySet().toArray(String[]::new))
                .usingGeneratedKeyColumns("id").executeAndReturnKey(valores).longValue();
    }

    public long insertarDetalle(Map<String, ?> datos) {
        return new SimpleJdbcInsert(jdbc).withTableName("factura_detalle")
                .usingColumns(datos.keySet().toArray(String[]::new))
                .usingGeneratedKeyColumns("id").executeAndReturnKey(datos).longValue();
    }

    public void insertarDescuento(long id, long usuarioId, String motivo) {
        jdbc.update("INSERT INTO factura_descuento (factura_id, usuario_id, motivo) VALUES (?, ?, ?)",
                id, usuarioId, motivo);
    }


    public record CitaFacturada(long id, long clienteId, long medicoId, long especializacionId) {
    }

    public record Servicio(long id, String nombre, BigDecimal precio, BigDecimal ivaPorcentaje) {
    }

    public record Producto(long id, String nombre, int cantidad, BigDecimal precio, Long despachoDetalleId,
                            BigDecimal ivaPorcentaje) {
        public BigDecimal subtotal() {
            return precio.multiply(BigDecimal.valueOf(cantidad));
        }
    }


    public boolean tieneDevoluciones(long facturaId) {
        return !jdbc.queryForList("""
                SELECT d.pago_id FROM devolucion_pago d JOIN pago p ON p.id = d.pago_id WHERE p.factura_id = ?
                """, facturaId).isEmpty();
    }

    public void insertarPago(long facturaId, BigDecimal monto, String metodo, String observacion, long usuarioId) {
        jdbc.update("""
                INSERT INTO pago (factura_id, monto, metodo_pago, observacion, usuario_id) VALUES (?, ?, ?, ?, ?)
                """, facturaId, monto, metodo, observacion, usuarioId);
    }

    public void actualizarEstado(long facturaId, String estado) {
        jdbc.update("UPDATE factura SET estado = ? WHERE id = ?", estado, facturaId);
    }

    public void anular(long facturaId, String motivo, long usuarioId) {
        jdbc.update("""
                UPDATE factura SET estado = 'ANULADA', motivo_anulacion = ?, usuario_anulacion = ?,
                    fecha_anulacion = CURRENT_TIMESTAMP WHERE id = ?
                """, motivo, usuarioId, facturaId);
    }

    public boolean pagoExiste(long pagoId, long facturaId) {
        return !jdbc.queryForList("SELECT id FROM pago WHERE id = ? AND factura_id = ?", pagoId, facturaId).isEmpty();
    }

    public boolean pagoDevuelto(long pagoId) {
        return !jdbc.queryForList("SELECT pago_id FROM devolucion_pago WHERE pago_id = ?", pagoId).isEmpty();
    }

    public void insertarDevolucion(long pagoId, String referencia, long usuarioId) {
        jdbc.update("INSERT INTO devolucion_pago (pago_id, referencia, usuario_id) VALUES (?, ?, ?)",
                pagoId, referencia, usuarioId);
    }

    public void insertarIva(long id, BigDecimal porcentaje, BigDecimal importe) {
        jdbc.update("INSERT INTO factura_detalle_iva (detalle_id, porcentaje, importe) VALUES (?, ?, ?)",
                id, porcentaje, importe);
    }

    public CitaFacturada bloquearCitaAtendida(long id) {
        return jdbc.query("""
                SELECT c.id, c.cliente_id, c.medico_id, m.especializacion_id
                FROM cita c JOIN medico m ON m.id = c.medico_id
                WHERE c.id = ? AND c.estado = 'ATENDIDA' FOR UPDATE OF c
                """, (rs, index) -> new CitaFacturada(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getLong(4)), id)
                .stream().findFirst().orElseThrow(() -> new BadRequestException("Se requiere una cita atendida."));
    }

    public boolean existeDocumento(long citaId, String tipo) {
        return !jdbc.queryForList("SELECT id FROM factura WHERE cita_id = ? AND tipo_documento = ? FOR UPDATE",
                citaId, tipo).isEmpty();
    }

    public FacturaSaldo bloquearFactura(long id) {
        return jdbc.query("SELECT total, estado FROM factura WHERE id = ? AND tipo_documento = 'FACTURA' FOR UPDATE",
                (rs, index) -> new FacturaSaldo(rs.getBigDecimal(1), rs.getString(2)), id)
                .stream().findFirst().orElseThrow(() -> new BadRequestException("La factura no existe."));
    }

    /** Suma los pagos vigentes y excluye los que ya tienen devolución. */
    public BigDecimal pagos(long id) {
        return jdbc.queryForObject("""
                SELECT COALESCE(SUM(p.monto), 0) FROM pago p LEFT JOIN devolucion_pago d ON d.pago_id = p.id
                WHERE p.factura_id = ? AND d.pago_id IS NULL
                """, BigDecimal.class, id);
    }

    public record FacturaSaldo(BigDecimal total, String estado) {}
    public Map<String, String> datosClinica() {
        return jdbc.query("""
                SELECT codigo, valor FROM parametro_sistema
                WHERE codigo IN ('CLINICA_NOMBRE', 'CLINICA_NIT', 'CLINICA_DIRECCION', 'CLINICA_TELEFONO', 'CLINICA_EMAIL')
                """, rs -> {
            var datos = new java.util.HashMap<String, String>();
            while (rs.next()) {
                datos.put(rs.getString("codigo"), rs.getString("valor"));
            }
            return datos;
        });
    }
}
