package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cita")
public interface CitaApi {

    /**
     * Filtra citas por rango de fecha (ISO-8601)
     * Ejemplo de formato: 2026-09-14T08:30:00
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Cita>> listarCitas() throws BadRequestException;

    @GetMapping(value = "/filtrar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Cita>> filtrarCitas(
            @RequestParam String fechaInicial,
            @RequestParam String fechaFinal)
            throws BadRequestException;
}
