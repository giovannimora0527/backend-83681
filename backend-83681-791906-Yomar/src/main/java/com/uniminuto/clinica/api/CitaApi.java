package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cita")
public interface CitaApi {

    /**
     * Filtra las citas del sistema entre una fecha inicial y una fecha
     * final, ordenadas desde la más reciente hasta la más antigua.
     *
     * @param fechaInicial fecha inicial del filtro (formato ISO, ej. 2026-01-01T00:00:00).
     * @param fechaFinal   fecha final del filtro (formato ISO, ej. 2026-12-31T23:59:59).
     * @return respuesta HTTP 200 con la lista de citas filtradas.
     * @throws BadRequestException excepción de validación.
     */
    @GetMapping(value = "/filtrar",
            produces = {"application/json"})
    ResponseEntity<List<Cita>> filtrarCitasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFinal)
            throws BadRequestException;

    /**
     * Registra una nueva cita en el sistema.
     *
     * @param citaRq datos de la cita a crear.
     * @return respuesta HTTP 201 con el resultado de la operación.
     * @throws BadRequestException excepción de validación.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarCita(
            @RequestBody CitaRq citaRq)
            throws BadRequestException;

    /**
     * Actualiza una cita ya existente en el sistema.
     *
     * @param citaRq datos actualizados de la cita.
     * @return respuesta HTTP 200 con el resultado de la operación.
     * @throws BadRequestException excepción de validación.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarCita(
            @RequestBody CitaRq citaRq)
            throws BadRequestException;
}