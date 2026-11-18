package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.DescuentoRq;
import com.uniminuto.clinica.models.PagoRq;
import com.uniminuto.clinica.models.AnulacionRq;
import com.uniminuto.clinica.models.DevolucionRq;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Expone la emisión, consulta y descarga de facturas definitivas, el registro de pagos y el
 * proceso de devolución y anulación.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/factura")
public interface FacturaApi {

    /**
     * Emite la factura definitiva de una cita atendida. El descuento es opcional y requiere
     * autorización administrativa.
     */
    @PostMapping("/cita/{citaId}")
    Map<String, Long> emitir(@PathVariable long citaId, Principal usuario,
                                   @RequestBody(required = false) DescuentoRq descuento);

    /** Registra un abono y devuelve el estado de pago resultante. */
    @PostMapping("/{id}/pagos")
    Map<String, String> pagar(@PathVariable long id, @RequestBody PagoRq pago, Principal usuario);

    /** Lista los pagos de la factura y sus referencias de devolución cuando existan. */
    @GetMapping("/{id}/pagos")
    List<Map<String, Object>> pagos(@PathVariable long id);

    /** Solicita la anulación con un motivo. Solo Administración puede realizarla. */
    @PostMapping("/{id}/anular")
    ResponseEntity<Void> anular(@PathVariable long id, @RequestBody AnulacionRq solicitud, Principal usuario);

    /** Registra el comprobante de devolución de un pago de la factura. */
    @PostMapping("/{id}/pagos/{pagoId}/devolucion")
    ResponseEntity<Void> devolver(@PathVariable long id, @PathVariable long pagoId,
                                         @RequestBody DevolucionRq solicitud, Principal usuario);

    /** Consulta facturas por propietario o mascota. Debe indicarse al menos uno de los dos filtros. */
    @GetMapping("/historial")
    List<Map<String, Object>> historial(@RequestParam(required = false) Long clienteId,
                                              @RequestParam(required = false) Long mascotaId);

    /** Descarga el PDF del documento con sus conceptos, importes y estado de pago. */
    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    ResponseEntity<byte[]> pdf(@PathVariable long id) throws IOException;
}
