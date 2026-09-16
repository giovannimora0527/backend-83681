package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/historia-medica")
public interface HistoriaMedicaApi {

    /**
     * Registra una nueva historia médica en el sistema.
     *
     * @param historiaMedicaRq datos de la historia médica a crear.
     * @return respuesta HTTP 201 con el resultado de la operación.
     * @throws BadRequestException excepción de validación.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearHistoriaMedica(
            @RequestBody HistoriaMedicaRq historiaMedicaRq)
            throws BadRequestException;

    /**
     * Lista las historias médicas entre una fecha inicial y una fecha
     * final, ordenadas desde la más reciente hasta la más antigua.
     *
     * @param fechaInicial fecha inicial del filtro (formato ISO, ej. 2026-01-01T00:00:00).
     * @param fechaFinal   fecha final del filtro (formato ISO, ej. 2026-12-31T23:59:59).
     * @return respuesta HTTP 200 con la lista de historias médicas filtradas.
     * @throws BadRequestException excepción de validación.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<HistoriaMedica>> listarHistoriasMedicas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFinal)
            throws BadRequestException;

    /**
     * Actualiza una historia médica ya existente en el sistema.
     *
     * @param historiaMedicaRq datos actualizados de la historia médica.
     * @return respuesta HTTP 200 con el resultado de la operación.
     * @throws BadRequestException excepción de validación.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarHistoriaMedica(
            @RequestBody HistoriaMedicaRq historiaMedicaRq)
            throws BadRequestException;
}