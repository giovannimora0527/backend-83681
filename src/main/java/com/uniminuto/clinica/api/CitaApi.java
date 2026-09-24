package com.uniminuto.clinica.api;


import com.uniminuto.clinica.entity.CitaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cita")
public interface CitaApi {

    @GetMapping(value = "/filtrar-by-fechas",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<CitaMedica>> getFormulasByOrden(
            @RequestParam LocalDateTime fechaInicio,
            @RequestParam LocalDateTime fechaFinal
    )
            throws BadRequestException;
}
