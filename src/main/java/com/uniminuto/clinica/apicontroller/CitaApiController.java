package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.CitaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador de las citas médicas: delega toda la lógica en {@link CitaService}.
 */
@RestController
public class CitaApiController implements CitaApi {

    /** Servicio con la lógica de negocio de citas. */
    @Autowired
    private CitaService citaService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<CitaRs>> listar() {
        return ResponseEntity.ok(citaService.listar());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<CitaRs> consultar(Long id) {
        return ResponseEntity.ok(citaService.consultar(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<CitaRs>> listarPorMascota(Long mascotaId) {
        return ResponseEntity.ok(citaService.listarPorMascota(mascotaId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> crear(CitaRq rq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(citaService.crear(rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> editar(Long id, CitaRq rq) {
        return ResponseEntity.ok(citaService.editar(id, rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> reactivar(Long id) {
        return ResponseEntity.ok(citaService.reactivar(id));
    }
}
