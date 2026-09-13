package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.CitaRs;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController

public class CitaApiController implements CitaApi {

    @Autowired
    private CitaService citaService;

    @Override
    public ResponseEntity<List<CitaRs>> filtrarCitas(LocalDate fechaInicio, LocalDate fechaFin) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.filtrarCitasPorFecha(fechaInicio, fechaFin));
    }

    @Override
    public ResponseEntity<CitaRs> crearCita(CitaRq citaRq) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.crearCita(citaRq));
    }

    @Override
    public ResponseEntity<CitaRs> actualizarCita(CitaRq citaRq) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.actualizarCita(citaRq));
    }
}
