package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.models.CambioEstadoCitaRq;
import com.uniminuto.clinica.models.CitaFiltroRq;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import com.uniminuto.clinica.models.ReactivarCitaRq;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST que implementa {@link CitaApi}. Solo delega en el
 * servicio; las reglas de negocio viven en {@link CitaService}.
 */
@RestController
public class CitaApiController implements CitaApi {

    /**
     * Servicio de citas.
     */
    private final CitaService citaService;

    public CitaApiController(CitaService citaService) {
        this.citaService = citaService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<PaginaRs<Cita>> listarCitas(CitaFiltroRq filtro) {
        return ResponseEntity.ok(citaService.listarCitas(filtro));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Cita> consultarCita(Long citaId) {
        return ResponseEntity.ok(citaService.consultarCita(citaId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> crearCita(CitaRq citaRq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(citaService.crearCita(citaRq));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> editarCita(Long citaId, CitaRq citaRq) {
        return ResponseEntity.ok(citaService.editarCita(citaId, citaRq));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> cambiarEstado(Long citaId, CambioEstadoCitaRq rq) {
        return ResponseEntity.ok(citaService.cambiarEstado(citaId, rq));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> reactivarCita(Long citaId, ReactivarCitaRq rq) {
        return ResponseEntity.ok(citaService.reactivarCita(citaId, rq));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<Cita>> listarAtendidasSinHistoria(Long mascotaId) {
        return ResponseEntity.ok(citaService.listarAtendidasSinHistoria(mascotaId));
    }
}
