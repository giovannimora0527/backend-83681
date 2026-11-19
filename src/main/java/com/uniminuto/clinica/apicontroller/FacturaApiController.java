package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FacturaApi;
import com.uniminuto.clinica.models.DescuentoRq;
import com.uniminuto.clinica.models.PagoRq;
import com.uniminuto.clinica.models.AnulacionRq;
import com.uniminuto.clinica.models.DevolucionRq;
import com.uniminuto.clinica.service.FacturacionService;
import com.uniminuto.clinica.util.FacturaPdf;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Recibe las solicitudes de facturación, identifica al usuario que realiza cada operación y
 * entrega el PDF de la factura.
 */
@RestController
@RequiredArgsConstructor
public class FacturaApiController implements FacturaApi {

    private final FacturacionService facturacion;

    /** Devuelve si la cita se puede facturar y, cuando se bloquea, indica por qué. */
    @Override
    public EstadoEmision estadoEmision(long citaId) {
        String motivo = facturacion.impedimentoEmision(citaId);
        return new EstadoEmision(motivo == null, motivo);
    }

    /** Solicita emitir la factura para la atención indicada. */
    @Override
    public Map<String, Long> emitir(long citaId, Principal usuario,
                                   DescuentoRq descuento) {
        return Map.of("facturaId", facturacion.facturar(citaId, Long.parseLong(usuario.getName()), descuento));
    }

    /** Registra el pago recibido para la factura indicada. */
    @Override
    public Map<String, String> pagar(long id, PagoRq pago, Principal usuario) {
        return Map.of("estado", facturacion.pagar(id, pago.monto(), pago.metodo(), pago.observacion(),
                Long.parseLong(usuario.getName())));
    }

    /** Devuelve los pagos asociados a la factura. */
    @Override
    public List<Map<String, Object>> pagos(long id) {
        return facturacion.listarPagos(id);
    }

    /** Registra la anulación de la factura junto con su motivo. */
    @Override
    public ResponseEntity<Void> anular(long id, AnulacionRq solicitud, Principal usuario) {
        facturacion.anular(id, solicitud.motivo(), Long.parseLong(usuario.getName()));
        return ResponseEntity.noContent().build();
    }

    /** Registra una devolución para el pago indicado. */
    @Override
    public ResponseEntity<Void> devolver(long id, long pagoId,
                                         DevolucionRq solicitud, Principal usuario) {
        facturacion.devolver(id, pagoId, solicitud.referencia(), Long.parseLong(usuario.getName()));
        return ResponseEntity.noContent().build();
    }

    /** Consulta el historial de la factura. */
    @Override
    public List<Map<String, Object>> historial(Long clienteId,
                                              Long mascotaId) {
        return facturacion.historial(clienteId, mascotaId);
    }

    /** Prepara la respuesta con el documento PDF solicitado. */
    @Override
    public ResponseEntity<byte[]> pdf(long id) throws IOException {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=factura-" + id + ".pdf")
                .body(FacturaPdf.generar(facturacion.documento(id, null)));
    }
}
