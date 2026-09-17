package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.AnotacionHistoriaApi;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.AnotacionHistoriaRs;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class AnotacionHistoriaApiController implements AnotacionHistoriaApi {

    @Autowired
    private AnotacionHistoriaService anotacionService;

    @Override
    public ResponseEntity<List<AnotacionHistoriaRs>> listarAnotaciones(LocalDate fechaInicio, LocalDate fechaFin) throws BadRequestException {
        return ResponseEntity.ok(this.anotacionService.listarAnotaciones(fechaInicio, fechaFin));
    }

    @Override
    public ResponseEntity<AnotacionHistoriaRs> crearAnotacion(AnotacionHistoriaRq anotacionRq) throws BadRequestException {
        return ResponseEntity.ok(this.anotacionService.crearAnotacion(anotacionRq));
    }

    @Override
    public ResponseEntity<AnotacionHistoriaRs> actualizarAnotacion(AnotacionHistoriaRq anotacionRq) throws BadRequestException {
        return ResponseEntity.ok(this.anotacionService.actualizarAnotacion(anotacionRq));
    }
}
