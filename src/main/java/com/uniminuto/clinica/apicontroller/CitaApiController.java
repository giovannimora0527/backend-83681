package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.entity.CitaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class CitaApiController implements CitaApi {

    @Autowired
    private CitaService citaService;

    @Override
    public ResponseEntity<List<CitaMedica>> getFormulasByOrden(LocalDateTime fechaInicio, LocalDateTime fechaFinal) throws BadRequestException {
        return ResponseEntity.ok(citaService.filtrarCitasPorFecha(fechaInicio, fechaFinal));
    }
}
