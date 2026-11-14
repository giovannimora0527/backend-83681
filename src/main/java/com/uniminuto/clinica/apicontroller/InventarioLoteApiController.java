package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.InventarioLoteApi;
import com.uniminuto.clinica.entity.InventarioLote;
import com.uniminuto.clinica.models.InventarioLoteActualizacionRq;
import com.uniminuto.clinica.models.InventarioLoteRq;
import com.uniminuto.clinica.models.InventarioLoteRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.InventarioLoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
public class InventarioLoteApiController implements InventarioLoteApi {

    private final InventarioLoteService inventarioLoteService;

    /** Construye el controlador con el servicio de lotes de inventario. */
    public InventarioLoteApiController(InventarioLoteService inventarioLoteService) {
        this.inventarioLoteService = inventarioLoteService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<InventarioLoteRs>> listarLotes() {
        List<InventarioLoteRs> lotes = inventarioLoteService.listarLotes().stream()
                .map(this::mapearRespuesta)
                .toList();
        return ResponseEntity.ok(lotes);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<InventarioLoteRs>> listarLotesPorMedicamento(Long medicamentoId) {
        List<InventarioLoteRs> lotes = inventarioLoteService.listarLotesPorMedicamento(medicamentoId).stream()
                .map(this::mapearRespuesta)
                .toList();
        return ResponseEntity.ok(lotes);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<InventarioLoteRs> consultarLote(Long id) {
        return ResponseEntity.ok(mapearRespuesta(inventarioLoteService.consultarLote(id)));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> crearLote(
            InventarioLoteRq solicitud,
            @AuthenticationPrincipal String usuarioResponsableId) {
        return ResponseEntity.ok(inventarioLoteService.guardarLote(solicitud, usuarioResponsableId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarLote(
            Long id,
            InventarioLoteActualizacionRq solicitud) {
        return ResponseEntity.ok(inventarioLoteService.actualizarLote(id, solicitud));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> desactivarLote(Long id) {
        return ResponseEntity.ok(inventarioLoteService.desactivarLote(id));
    }

    /** Convierte un lote de inventario a su representación de respuesta. */
    private InventarioLoteRs mapearRespuesta(InventarioLote lote) {
        InventarioLoteRs respuesta = new InventarioLoteRs();
        respuesta.setId(lote.getId());
        respuesta.setMedicamentoId(lote.getMedicamento() == null ? null : lote.getMedicamento().getId());
        respuesta.setNumeroLote(lote.getNumeroLote());
        respuesta.setCantidad(lote.getCantidad());
        respuesta.setPrecioCompraLote(lote.getPrecioCompraLote());
        respuesta.setFechaVencimiento(lote.getFechaVencimiento());
        respuesta.setFechaIngreso(lote.getFechaIngreso());
        respuesta.setActivo(lote.getActivo());
        return respuesta;
    }
}
