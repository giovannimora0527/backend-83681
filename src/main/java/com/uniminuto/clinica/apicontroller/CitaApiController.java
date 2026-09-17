// apicontroller/CitaApiController.java
package com.uniminuto.clinica.apicontroller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.CitaService;

@RestController
public class CitaApiController implements CitaApi {

    @Autowired
    private CitaService citaService;

    @Override
    public ResponseEntity<List<Cita>> filtrarPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws BadRequestException {
        return ResponseEntity.ok(citaService.filtrarPorFecha(fechaInicio, fechaFin));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarCita(CitaRq citaRq) throws BadRequestException {
        return ResponseEntity.ok(citaService.guardarCita(citaRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarCita(CitaRq citaRq) throws BadRequestException {
        return ResponseEntity.ok(citaService.actualizarCita(citaRq));
    }
}