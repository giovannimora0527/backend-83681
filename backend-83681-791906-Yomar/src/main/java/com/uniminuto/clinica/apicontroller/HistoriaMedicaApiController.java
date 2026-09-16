package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.HistoriaMedicaApi;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.HistoriaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class HistoriaMedicaApiController implements HistoriaMedicaApi {

    @Autowired
    private HistoriaMedicaService historiaMedicaService;

    @Override
    public ResponseEntity<MiRespuestaRS> crearHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.historiaMedicaService.crearHistoriaMedica(historiaMedicaRq));
    }

    @Override
    public ResponseEntity<List<HistoriaMedica>> listarHistoriasMedicas(LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.listarHistoriasMedicas(fechaInicial, fechaFinal));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.actualizarHistoriaMedica(historiaMedicaRq));
    }
}