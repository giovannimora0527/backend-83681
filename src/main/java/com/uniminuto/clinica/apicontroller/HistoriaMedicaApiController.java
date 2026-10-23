package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.HistoriaMedicaApi;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.models.HistoriaMedicaFiltroRq;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import com.uniminuto.clinica.service.HistoriaMedicaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST que implementa {@link HistoriaMedicaApi}.
 */
@RestController
public class HistoriaMedicaApiController implements HistoriaMedicaApi {

    /**
     * Servicio de historias clínicas.
     */
    private final HistoriaMedicaService historiaMedicaService;

    public HistoriaMedicaApiController(HistoriaMedicaService historiaMedicaService) {
        this.historiaMedicaService = historiaMedicaService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<PaginaRs<HistoriaMedica>> listarHistorias(HistoriaMedicaFiltroRq filtro) {
        return ResponseEntity.ok(historiaMedicaService.listarHistorias(filtro));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<HistoriaMedica> consultarHistoria(Long historiaId) {
        return ResponseEntity.ok(historiaMedicaService.consultarHistoria(historiaId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> crearHistoria(HistoriaMedicaRq rq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historiaMedicaService.crearHistoria(rq));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> cerrarHistoria(Long historiaId) {
        return ResponseEntity.ok(historiaMedicaService.cerrarHistoria(historiaId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<HistoriaMedica>> listarPorMascota(Long mascotaId) {
        return ResponseEntity.ok(historiaMedicaService.listarPorMascota(mascotaId));
    }
}
