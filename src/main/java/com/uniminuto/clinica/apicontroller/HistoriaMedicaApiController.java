package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.HistoriaMedicaApi;
import com.uniminuto.clinica.models.AnotacionRq;
import com.uniminuto.clinica.models.AnotacionRs;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.HistoriaMedicaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.HistoriaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador de las historias clínicas: delega toda la lógica en {@link HistoriaMedicaService}.
 */
@RestController
public class HistoriaMedicaApiController implements HistoriaMedicaApi {

    /** Servicio con la lógica de negocio de historias y anotaciones. */
    @Autowired
    private HistoriaMedicaService historiaService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<HistoriaMedicaRs>> listar() {
        return ResponseEntity.ok(historiaService.listar());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<HistoriaMedicaRs> consultar(Long id) {
        return ResponseEntity.ok(historiaService.consultar(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<HistoriaMedicaRs>> listarPorMascota(Long mascotaId) {
        return ResponseEntity.ok(historiaService.listarPorMascota(mascotaId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> crear(HistoriaMedicaRq rq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historiaService.crear(rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> cerrar(Long id) {
        return ResponseEntity.ok(historiaService.cerrar(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<AnotacionRs>> listarAnotaciones(Long id) {
        return ResponseEntity.ok(historiaService.listarAnotaciones(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<AnotacionRs> consultarAnotacion(Long anotacionId) {
        return ResponseEntity.ok(historiaService.consultarAnotacion(anotacionId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> crearAnotacion(Long id, AnotacionRq rq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historiaService.crearAnotacion(id, rq));
    }
}
