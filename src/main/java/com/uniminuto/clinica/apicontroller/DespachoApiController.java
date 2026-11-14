package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.DespachoApi;
import com.uniminuto.clinica.entity.Despacho;
import com.uniminuto.clinica.entity.DespachoDetalle;
import com.uniminuto.clinica.entity.DespachoHistorialEstado;
import com.uniminuto.clinica.models.DespachoCancelarRq;
import com.uniminuto.clinica.models.DespachoDetalleRs;
import com.uniminuto.clinica.models.DespachoHistorialEstadoRs;
import com.uniminuto.clinica.models.DespachoRq;
import com.uniminuto.clinica.models.DespachoRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.DespachoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DespachoApiController implements DespachoApi {

    private final DespachoService despachoService;

    public DespachoApiController(DespachoService despachoService) {
        this.despachoService = despachoService;
    }

    @Override
    public ResponseEntity<List<DespachoRs>> listarDespachos() {
        List<DespachoRs> despachos = despachoService.listarDespachos().stream()
                .map(this::mapearDespacho)
                .toList();
        return ResponseEntity.ok(despachos);
    }

    @Override
    public ResponseEntity<DespachoRs> consultarDespacho(Long despachoId) {
        return ResponseEntity.ok(mapearDespacho(despachoService.consultarDespacho(despachoId)));
    }

    @Override
    public ResponseEntity<List<DespachoRs>> listarDespachosPorFormula(Long formulaId) {
        List<DespachoRs> despachos = despachoService.listarDespachosPorFormula(formulaId).stream()
                .map(this::mapearDespacho)
                .toList();
        return ResponseEntity.ok(despachos);
    }

    @Override
    public ResponseEntity<List<DespachoDetalleRs>> listarDetalles(Long despachoId) {
        List<DespachoDetalleRs> detalles = despachoService.listarDetalles(despachoId).stream()
                .map(this::mapearDetalle)
                .toList();
        return ResponseEntity.ok(detalles);
    }

    @Override
    public ResponseEntity<List<DespachoHistorialEstadoRs>> listarHistorial(Long despachoId) {
        List<DespachoHistorialEstadoRs> historial = despachoService.listarHistorial(despachoId).stream()
                .map(this::mapearHistorial)
                .toList();
        return ResponseEntity.ok(historial);
    }

    @Override
    public ResponseEntity<DespachoRs> guardarDespacho(DespachoRq despachoRq, String usuarioResponsableId) {
        Despacho despacho = despachoService.guardarDespacho(despachoRq, usuarioResponsableId);
        return ResponseEntity.ok(mapearDespacho(despacho));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> cancelarDespacho(
            Long despachoId,
            DespachoCancelarRq cancelacionRq,
            String usuarioResponsableId) {
        return ResponseEntity.ok(
                despachoService.cancelarDespacho(despachoId, cancelacionRq, usuarioResponsableId));
    }

    private DespachoRs mapearDespacho(Despacho despacho) {
        DespachoRs respuesta = new DespachoRs();
        respuesta.setId(despacho.getId());
        respuesta.setNumeroDespacho(despacho.getNumeroDespacho());
        respuesta.setNumeroComprobante(despacho.getNumeroComprobante());
        respuesta.setUrlComprobantePdf(despacho.getUrlComprobantePdf());
        respuesta.setFechaGeneracionComprobante(despacho.getFechaGeneracionComprobante());
        respuesta.setFormulaId(despacho.getFormulaId());
        respuesta.setCitaId(despacho.getCitaId());
        respuesta.setClienteId(despacho.getClienteId());
        respuesta.setMascotaId(despacho.getMascotaId());
        respuesta.setUsuarioDespachoId(despacho.getUsuarioDespacho() == null
                ? null : despacho.getUsuarioDespacho().getId());
        respuesta.setFechaDespacho(despacho.getFechaDespacho());
        respuesta.setEstado(despacho.getEstado());
        respuesta.setObservacion(despacho.getObservacion());
        return respuesta;
    }

    private DespachoDetalleRs mapearDetalle(DespachoDetalle detalle) {
        DespachoDetalleRs respuesta = new DespachoDetalleRs();
        respuesta.setId(detalle.getId());
        respuesta.setDespachoId(detalle.getDespacho() == null ? null : detalle.getDespacho().getId());
        respuesta.setFormulaDetalleId(detalle.getFormulaDetalleId());
        respuesta.setMedicamentoId(detalle.getMedicamento() == null ? null : detalle.getMedicamento().getId());
        respuesta.setLoteId(detalle.getLote() == null ? null : detalle.getLote().getId());
        respuesta.setCantidadEntregada(detalle.getCantidadEntregada());
        respuesta.setCantidadFormulada(detalle.getCantidadFormulada());
        return respuesta;
    }

    private DespachoHistorialEstadoRs mapearHistorial(DespachoHistorialEstado historial) {
        DespachoHistorialEstadoRs respuesta = new DespachoHistorialEstadoRs();
        respuesta.setId(historial.getId());
        respuesta.setDespachoId(historial.getDespacho() == null ? null : historial.getDespacho().getId());
        respuesta.setEstadoAnterior(historial.getEstadoAnterior());
        respuesta.setEstadoNuevo(historial.getEstadoNuevo());
        respuesta.setUsuarioId(historial.getUsuario() == null ? null : historial.getUsuario().getId());
        respuesta.setObservacion(historial.getObservacion());
        respuesta.setFechaCambio(historial.getFechaCambio());
        return respuesta;
    }
}
