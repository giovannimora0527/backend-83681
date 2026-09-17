// apicontroller/AnotacionHistoriaApiController.java
package com.uniminuto.clinica.apicontroller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.uniminuto.clinica.api.AnotacionHistoriaApi;
import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.AnotacionHistoriaService;

@RestController
public class AnotacionHistoriaApiController implements AnotacionHistoriaApi {

    @Autowired
    private AnotacionHistoriaService anotacionHistoriaService;

    @Override
    public ResponseEntity<List<AnotacionHistoria>> listarPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws BadRequestException {
        return ResponseEntity.ok(anotacionHistoriaService.listarPorFecha(fechaInicio, fechaFin));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardar(AnotacionHistoriaRq rq) throws BadRequestException {
        return ResponseEntity.ok(anotacionHistoriaService.guardarAnotacion(rq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizar(AnotacionHistoriaRq rq) throws BadRequestException {
        return ResponseEntity.ok(anotacionHistoriaService.actualizarAnotacion(rq));
    }
}