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
     * Filtra las citas del sistema dado un rango de fechas.
     * Resuelve los puntos 2 y 3 del taller.
     *
     * @param fechaInicio fecha y hora inicial del rango, formato ISO (ej: 2024-06-01T00:00:00).
     * @param fechaFin    fecha y hora final del rango, formato ISO (ej: 2024-06-30T23:59:59).
     * @return lista de citas dentro del rango, de la más reciente a la más antigua.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/filtrar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Cita>> filtrarCitasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin)
            throws BadRequestException;

    /**
     * Registra una nueva cita en el sistema.
     *
     * @param citaRq datos de la cita a crear.
     * @return mensaje de confirmación.
     * @throws BadRequestException excepcion.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarCita(
            @RequestBody CitaRq citaRq)
            throws BadRequestException;

    /**
     * Actualiza una cita ya almacenada en el sistema.
     *
     * @param citaRq datos nuevos de la cita, incluyendo su id.
     * @return mensaje de confirmación.
     * @throws BadRequestException excepcion.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarCita(
            @RequestBody CitaRq citaRq)
            throws BadRequestException;
}