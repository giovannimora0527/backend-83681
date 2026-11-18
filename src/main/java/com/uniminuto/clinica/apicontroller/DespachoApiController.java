package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.DespachoApi;
import com.uniminuto.clinica.entity.Despacho;
import com.uniminuto.clinica.entity.DespachoDetalle;
import com.uniminuto.clinica.entity.DespachoHistorialEstado;
import com.uniminuto.clinica.entity.FormulaDetalle;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.InventarioLote;
import com.uniminuto.clinica.models.DespachoCancelarRq;
import com.uniminuto.clinica.models.DespachoHistorialEstadoRs;
import com.uniminuto.clinica.models.DespachoRq;
import com.uniminuto.clinica.models.DespachoRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.DespachoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Adapta las solicitudes HTTP del despacho a los casos de uso del servicio. */
@RestController
public class DespachoApiController implements DespachoApi {

    private final DespachoService despachoService;

    /** Construye el controlador con el servicio de despacho. */
    public DespachoApiController(DespachoService despachoService) {
        this.despachoService = despachoService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<DespachoRs>> listarDespachos(String usuarioResponsableId) {
        List<DespachoRs> despachos = despachoService.listarDespachos(usuarioResponsableId).stream()
                .map(this::mapearDespacho)
                .toList();
        return ResponseEntity.ok(despachos);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<DespachoRs> consultarDespacho(Long despachoId, String usuarioResponsableId) {
        return ResponseEntity.ok(mapearDespacho(
                despachoService.consultarDespacho(despachoId, usuarioResponsableId)));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<DespachoRs>> listarDespachosPorFormula(Long formulaId,
                                                                       String usuarioResponsableId) {
        List<DespachoRs> despachos = despachoService
                .listarDespachosPorFormula(formulaId, usuarioResponsableId).stream()
                .map(this::mapearDespacho)
                .toList();
        return ResponseEntity.ok(despachos);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<DetalleTrazabilidadRs>> listarDetalles(Long despachoId,
                                                                       String usuarioResponsableId) {
        return ResponseEntity.ok(despachoService.listarDetalles(despachoId, usuarioResponsableId).stream()
                .map(this::mapearDetalleTrazabilidad)
                .toList());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<DespachoHistorialEstadoRs>> listarHistorial(Long despachoId,
                                                                            String usuarioResponsableId) {
        return ResponseEntity.ok(despachoService.listarHistorial(despachoId, usuarioResponsableId).stream()
                .map(this::mapearHistorialEstado)
                .toList());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<FormulaPendienteRs>> listarFormulasPendientes(
            String filtro, LocalDate fechaGeneracion, String usuarioResponsableId) {
        return ResponseEntity.ok(despachoService.listarFormulasPendientes(
                        filtro, fechaGeneracion, usuarioResponsableId).stream()
                .map(this::mapearFormulaPendiente)
                .toList());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<FormulaPendienteRs> consultarPreparacionFormula(Long formulaId,
                                                                           String usuarioResponsableId) {
        return ResponseEntity.ok(mapearFormulaPendiente(
                despachoService.consultarFormulaParaDespacho(formulaId, usuarioResponsableId)));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<DespachoHistorialRs>> consultarHistorialDespachos(
            String filtro, LocalDate fechaDesde, LocalDate fechaHasta, String usuarioResponsableId) {
        return ResponseEntity.ok(despachoService
                .consultarHistorialDespachos(filtro, fechaDesde, fechaHasta, usuarioResponsableId).stream()
                .map(this::mapearHistorialDespacho)
                .toList());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<DetalleTrazabilidadRs>> consultarHistorialMedicamento(Long medicamentoId,
                                                                                      String usuarioResponsableId) {
        return ResponseEntity.ok(despachoService
                .consultarHistorialMedicamento(medicamentoId, usuarioResponsableId).stream()
                .map(this::mapearDetalleTrazabilidad)
                .toList());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<DespachoRs> prepararDespacho(DespachoRq despachoRq, String usuarioResponsableId) {
        return ResponseEntity.ok(mapearDespacho(
                despachoService.prepararDespacho(despachoRq, usuarioResponsableId)));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<DespachoRs> confirmarDespacho(Long despachoId,
                                                         ConfirmacionDespachoRq confirmacionRq,
                                                         String usuarioResponsableId) {
        List<DespachoService.LineaConfirmacion> detalles = confirmacionRq == null
                || confirmacionRq.getDetalles() == null
                ? List.of()
                : confirmacionRq.getDetalles().stream()
                .map(detalle -> new DespachoService.LineaConfirmacion(
                        detalle == null ? null : detalle.getFormulaDetalleId(),
                        detalle == null ? null : detalle.getLoteId(),
                        detalle == null ? null : detalle.getCantidadEntregada()))
                .toList();
        return ResponseEntity.ok(mapearDespacho(despachoService.confirmarDespacho(
                despachoId,
                detalles,
                confirmacionRq == null ? null : confirmacionRq.getClaveIdempotencia(),
                confirmacionRq == null ? null : confirmacionRq.getObservacion(),
                usuarioResponsableId)));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> cancelarDespacho(Long despachoId,
                                                           DespachoCancelarRq cancelacionRq,
                                                           String usuarioResponsableId) {
        return ResponseEntity.ok(despachoService.cancelarDespacho(
                despachoId, cancelacionRq, usuarioResponsableId));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> cerrarPendientes(Long despachoId, DespachoCancelarRq solicitud,
                                                         String usuarioResponsableId) {
        return ResponseEntity.ok(despachoService.cerrarPendientes(despachoId, solicitud, usuarioResponsableId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<byte[]> descargarComprobante(Long despachoId, String usuarioResponsableId) {
        byte[] pdf = despachoService.generarComprobantePdf(despachoId, usuarioResponsableId);
        String nombreArchivo = "comprobante-despacho-" + despachoId + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .body(pdf);
    }

    /** Convierte el encabezado de despacho al contrato HTTP existente. */
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

    /** Convierte una línea de despacho sin exponer entidades JPA en la respuesta. */
    private DetalleTrazabilidadRs mapearDetalleTrazabilidad(DespachoDetalle detalle) {
        DetalleTrazabilidadRs respuesta = new DetalleTrazabilidadRs();
        respuesta.setId(detalle.getId());
        respuesta.setDespachoId(detalle.getDespacho() == null ? null : detalle.getDespacho().getId());
        respuesta.setNumeroDespacho(detalle.getDespacho() == null ? null
                : detalle.getDespacho().getNumeroDespacho());
        respuesta.setFormulaId(detalle.getDespacho() == null ? null : detalle.getDespacho().getFormulaId());
        respuesta.setNumeroFormula(detalle.getDespacho() == null ? null
                : detalle.getDespacho().getNumeroFormula());
        respuesta.setFormulaDetalleId(detalle.getFormulaDetalleId());
        respuesta.setMedicamentoId(detalle.getMedicamento() == null ? null : detalle.getMedicamento().getId());
        respuesta.setMedicamentoNombre(detalle.getMedicamentoNombre());
        respuesta.setLoteId(detalle.getLote() == null ? null : detalle.getLote().getId());
        respuesta.setNumeroLote(detalle.getNumeroLote());
        respuesta.setFechaVencimiento(detalle.getFechaVencimientoLote());
        respuesta.setCantidadFormulada(detalle.getCantidadFormulada());
        respuesta.setCantidadEntregada(detalle.getCantidadEntregada());
        respuesta.setMovimientoInventarioId(detalle.getMovimientoInventarioId());
        respuesta.setMovimientoReversionId(detalle.getMovimientoReversionId());
        respuesta.setMovimientoFecha(detalle.getMovimientoFecha());
        respuesta.setMovimientoUsuarioId(detalle.getMovimientoUsuarioId());
        respuesta.setMovimientoUsuarioNombre(detalle.getMovimientoUsuarioNombre());
        respuesta.setMovimientoReversionFecha(detalle.getMovimientoReversionFecha());
        respuesta.setMovimientoReversionUsuarioId(detalle.getMovimientoReversionUsuarioId());
        respuesta.setMovimientoReversionUsuarioNombre(detalle.getMovimientoReversionUsuarioNombre());
        respuesta.setFechaDespacho(detalle.getDespacho() == null ? null : detalle.getDespacho().getFechaDespacho());
        respuesta.setEstadoDespacho(detalle.getEstadoDespacho());
        respuesta.setUsuarioDespachoId(detalle.getDespacho() == null
                || detalle.getDespacho().getUsuarioDespacho() == null
                ? null : detalle.getDespacho().getUsuarioDespacho().getId());
        respuesta.setUsuarioDespachoNombre(detalle.getDespacho() == null
                || detalle.getDespacho().getUsuarioDespacho() == null
                ? null : detalle.getDespacho().getUsuarioDespacho().getUsername());
        return respuesta;
    }

    /** Convierte el historial de cambios de estado a su contrato de respuesta. */
    private DespachoHistorialEstadoRs mapearHistorialEstado(DespachoHistorialEstado historial) {
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

    /** Convierte la fórmula enriquecida con disponibilidad al contrato de consulta. */
    private FormulaPendienteRs mapearFormulaPendiente(FormulaMedica formula) {
        FormulaPendienteRs respuesta = new FormulaPendienteRs();
        respuesta.setFormulaId(formula.getId());
        respuesta.setNumeroFormula(formula.getNumeroFormula());
        respuesta.setFechaGeneracion(formula.getFechaEmision());
        respuesta.setMedicoId(formula.getMedico() == null ? null : formula.getMedico().getId());
        respuesta.setMedicoNombre(formula.getMedicoNombre());
        respuesta.setPropietarioId(formula.getPropietarioId());
        respuesta.setPropietarioNombre(formula.getPropietarioNombre());
        respuesta.setMascotaId(formula.getMascotaId());
        respuesta.setMascotaNombre(formula.getMascotaNombre());
        respuesta.setIndicaciones(formula.getIndicaciones());
        respuesta.setEstadoDespacho(formula.getEstadoDispensacion());
        respuesta.setMedicamentos(formula.getDetallesDespacho() == null ? List.of()
                : formula.getDetallesDespacho().stream().map(this::mapearMedicamentoFormulado).toList());
        return respuesta;
    }

    /** Convierte la prescripción y sus lotes disponibles al contrato de consulta. */
    private MedicamentoFormuladoRs mapearMedicamentoFormulado(FormulaDetalle detalle) {
        MedicamentoFormuladoRs respuesta = new MedicamentoFormuladoRs();
        respuesta.setFormulaDetalleId(detalle.getId());
        respuesta.setMedicamentoId(detalle.getMedicamentoId());
        respuesta.setMedicamentoNombre(detalle.getMedicamentoNombre());
        respuesta.setDosis(detalle.getDosis());
        respuesta.setFrecuencia(detalle.getFrecuencia());
        respuesta.setDuracionTratamiento(detalle.getDuracionTratamiento());
        respuesta.setViaAdministracion(detalle.getViaAdministracion());
        respuesta.setIndicaciones(detalle.getIndicaciones());
        respuesta.setCantidadPrescrita(detalle.getCantidadPrescrita());
        respuesta.setCantidadDispensada(detalle.getCantidadDispensada());
        respuesta.setCantidadPendiente(detalle.getCantidadPendiente());
        respuesta.setCantidadDisponible(detalle.getCantidadDisponible());
        int pendiente = detalle.getCantidadPendiente() == null ? 0 : detalle.getCantidadPendiente();
        int disponible = detalle.getCantidadDisponible() == null ? 0 : detalle.getCantidadDisponible();
        respuesta.setCantidadFaltante(Math.max(0, pendiente - disponible));
        respuesta.setDisponibilidadSuficiente(disponible >= pendiente);
        List<LoteDisponibleRs> lotes = new java.util.ArrayList<>();
        boolean recomendadoAsignado = false;
        if (detalle.getLotesDisponibles() != null) {
            for (InventarioLote lote : detalle.getLotesDisponibles()) {
                LoteDisponibleRs loteRs = mapearLoteDisponible(lote);
                if (!recomendadoAsignado && loteRs.isSeleccionable()) {
                    loteRs.setRecomendadoFefo(true);
                    recomendadoAsignado = true;
                }
                lotes.add(loteRs);
            }
        }
        respuesta.setLotes(lotes);
        return respuesta;
    }

    /** Convierte un lote de inventario en la información requerida para seleccionar. */
    private LoteDisponibleRs mapearLoteDisponible(InventarioLote lote) {
        LoteDisponibleRs respuesta = new LoteDisponibleRs();
        LocalDate hoy = LocalDate.now();
        respuesta.setLoteId(lote.getId());
        respuesta.setNumeroLote(lote.getNumeroLote());
        respuesta.setCantidadDisponible(lote.getCantidad());
        respuesta.setFechaVencimiento(lote.getFechaVencimiento());
        respuesta.setVencido(lote.getFechaVencimiento().isBefore(hoy));
        respuesta.setProximoAVencer(!lote.getFechaVencimiento().isBefore(hoy)
                && !lote.getFechaVencimiento().isAfter(hoy.plusDays(30)));
        respuesta.setSeleccionable(Boolean.TRUE.equals(lote.getActivo())
                && lote.getCantidad() != null && lote.getCantidad() > 0
                && !lote.getFechaVencimiento().isBefore(hoy));
        respuesta.setRecomendadoFefo(false);
        return respuesta;
    }

    /** Convierte un despacho y sus líneas al formato completo de historial. */
    private DespachoHistorialRs mapearHistorialDespacho(Despacho despacho) {
        DespachoHistorialRs respuesta = new DespachoHistorialRs();
        respuesta.setId(despacho.getId());
        respuesta.setNumeroDespacho(despacho.getNumeroDespacho());
        respuesta.setNumeroComprobante(despacho.getNumeroComprobante());
        respuesta.setUrlComprobantePdf(despacho.getUrlComprobantePdf());
        respuesta.setFormulaId(despacho.getFormulaId());
        respuesta.setNumeroFormula(despacho.getNumeroFormula());
        respuesta.setFechaGeneracionFormula(despacho.getFechaGeneracionFormula());
        respuesta.setCitaId(despacho.getCitaId());
        respuesta.setClienteId(despacho.getClienteId());
        respuesta.setPropietarioNombre(despacho.getPropietarioNombre());
        respuesta.setMascotaId(despacho.getMascotaId());
        respuesta.setMascotaNombre(despacho.getMascotaNombre());
        respuesta.setMedicoNombre(despacho.getMedicoNombre());
        respuesta.setUsuarioDespachoId(despacho.getUsuarioDespacho() == null
                ? null : despacho.getUsuarioDespacho().getId());
        respuesta.setUsuarioDespachoNombre(despacho.getUsuarioDespacho() == null
                ? null : despacho.getUsuarioDespacho().getUsername());
        respuesta.setFechaDespacho(despacho.getFechaDespacho());
        respuesta.setEstado(despacho.getEstado());
        respuesta.setObservacion(despacho.getObservacion());
        respuesta.setDetalles(despacho.getDetallesTrazabilidad() == null ? List.of()
                : despacho.getDetallesTrazabilidad().stream().map(this::mapearDetalleTrazabilidad).toList());
        return respuesta;
    }
}
