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

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class AnotacionHistoriaApiController implements AnotacionHistoriaApi {

    @Autowired
    private AnotacionHistoriaService anotacionHistoriaService;

    @Override
    public ResponseEntity<MiRespuestaRS> guardarAnotacion(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException {
        return ResponseEntity.ok(this.anotacionHistoriaService.guardarAnotacion(anotacionHistoriaRq));
    }

    @Override
    public ResponseEntity<List<AnotacionHistoria>> getAnotaciones(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws BadRequestException {
        return ResponseEntity.ok(this.anotacionHistoriaService.listarAnotaciones(fechaInicio, fechaFin));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarAnotacion(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException {
        return ResponseEntity.ok(this.anotacionHistoriaService.actualizarAnotacion(anotacionHistoriaRq));
    }
}