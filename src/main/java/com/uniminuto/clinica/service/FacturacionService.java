package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.DescuentoRq;
import com.uniminuto.clinica.models.FacturaDocumento;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Define la emisión de documentos, el registro de pagos y el procedimiento de devolución y
 * anulación.
 */
public interface FacturacionService {
    /** Guarda los servicios prestados y los medicamentos prescritos al cierre de la atención. */
    long prefacturar(long citaId, List<Long> tarifaIds, long usuarioId);

    /** Emite la factura con los servicios de la prefactura y los medicamentos que se entregaron. */
    long facturar(long citaId, long usuarioId, DescuentoRq descuento);

    /** Devuelve el motivo por el que la cita no puede facturarse, o null si esta lista. */
    String impedimentoEmision(long citaId);

    /** Registra el abono dentro del saldo pendiente y devuelve el nuevo estado. */
    String pagar(long facturaId, BigDecimal monto, String metodo, String observacion, long usuarioId);

    /** Anula el documento conservando el historial y exige la devolución previa de los pagos. */
    void anular(long facturaId, String motivo, long usuarioId);

    /** Registra la devolución de un pago y su referencia. */
    void devolver(long facturaId, long pagoId, String referencia, long usuarioId);

    /** Consulta las facturas por propietario o mascota. */
    List<Map<String, Object>> historial(Long clienteId, Long mascotaId);

    /** Consulta los abonos y devoluciones del documento. */
    List<Map<String, Object>> listarPagos(long facturaId);

    /** Reúne los datos para descargar una factura o una prefactura. */
    FacturaDocumento documento(long facturaId, Long citaId);
}
