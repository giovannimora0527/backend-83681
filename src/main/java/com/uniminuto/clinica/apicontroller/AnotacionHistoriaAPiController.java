package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.AnotacionHistoriaApi;
import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador de anotaciones. Recibe la peticion HTTP y delega al servicio.
 */
@RestController
public class AnotacionHistoriaAPiController implements AnotacionHistoriaApi {

    /**
     * Servicio con la logica de negocio de las anotaciones.
     */
    @Autowired
    private AnotacionHistoriaService anotacionHistoriaService;

    @Override
    public ResponseEntity<MiRespuestaRS> guardarAnotacion(AnotacionRq anotacionRq) throws BadRequestException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(anotacionHistoriaService.crearAnotacionHistoria(anotacionRq));
    }

    @Override
    public ResponseEntity<List<AnotacionHistoria>> listarPorHistoria(Long historiaId) throws BadRequestException {
        return ResponseEntity.ok(anotacionHistoriaService.listarPorHistoria(historiaId));
    }

    @Override
    public ResponseEntity<List<AnotacionHistoria>> listarPorMascota(Long mascotaId) throws BadRequestException {
        return ResponseEntity.ok(anotacionHistoriaService.listarPorMascota(mascotaId));
    }
}
