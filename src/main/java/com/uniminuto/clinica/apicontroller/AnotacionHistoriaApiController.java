package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.AnotacionHistoriaApi;
import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.FiltroPaginadoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST que implementa {@link AnotacionHistoriaApi}.
 */
@RestController
public class AnotacionHistoriaApiController implements AnotacionHistoriaApi {

    /**
     * Servicio de anotaciones.
     */
    private final AnotacionHistoriaService anotacionService;

    public AnotacionHistoriaApiController(AnotacionHistoriaService anotacionService) {
        this.anotacionService = anotacionService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> crearAnotacion(AnotacionHistoriaRq rq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(anotacionService.crearAnotacion(rq));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<PaginaRs<AnotacionHistoria>> listarPorHistoria(Long historiaId, FiltroPaginadoRq filtro) {
        return ResponseEntity.ok(anotacionService.listarPorHistoria(historiaId, filtro));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<PaginaRs<AnotacionHistoria>> listarPorMascota(Long mascotaId, FiltroPaginadoRq filtro) {
        return ResponseEntity.ok(anotacionService.listarPorMascota(mascotaId, filtro));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<AnotacionHistoria> consultarAnotacion(Long anotacionId) {
        return ResponseEntity.ok(anotacionService.consultarAnotacion(anotacionId));
    }
}
