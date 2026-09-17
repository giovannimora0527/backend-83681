package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.AnotacionHistoriaApi;
import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST que implementa el contrato AnotacionHistoriaApi.
 */
@RestController
public class AnotacionHistoriaApiController implements AnotacionHistoriaApi {

    /** Servicio de negocio de las anotaciones de historia. */
    @Autowired
    private AnotacionHistoriaService anotacionHistoriaService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<AnotacionHistoria>> listarAnotaciones(Long historiaId)
            throws BadRequestException {
        return ResponseEntity.ok(
                this.anotacionHistoriaService.listarAnotaciones(historiaId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> guardarAnotacion(
            AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException {
        return ResponseEntity.ok(
                this.anotacionHistoriaService.guardarAnotacion(anotacionHistoriaRq));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarAnotacion(
            AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException {
        return ResponseEntity.ok(
                this.anotacionHistoriaService.actualizarAnotacion(anotacionHistoriaRq));
    }
}
