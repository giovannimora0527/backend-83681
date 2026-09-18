package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato REST para la gestión de citas de la Clínica Veterinaria.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cita")
public interface CitaApi {

    /**
     * Filtra las citas del sistema dada una fecha inicial y una fecha
     * final, ordenadas de la más reciente a la más antigua.
     *
     * @param fechaInicial fecha inicial del rango a filtrar.
     * @param fechaFinal   fecha final del rango a filtrar.
     * @return lista de citas encontradas.
     * @throws BadRequestException excepción en caso de error de negocio.
     */
    @GetMapping(value = "/filtrar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Cita>> filtrarCitasPorFecha(
            @RequestParam("fechaInicial") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicial,
            @RequestParam("fechaFinal") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFinal
    )
            throws BadRequestException;

    /**
     * Crea una nueva cita en el sistema.
     *
     * @param citaRq datos de la cita a crear.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException excepción en caso de error de negocio.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarCita(
            @RequestBody CitaRq citaRq
    )
            throws BadRequestException;

    /**
     * Actualiza una cita almacenada en el sistema.
     *
     * @param citaRq datos de la cita a actualizar.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException excepción en caso de error de negocio.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarCita(
            @RequestBody CitaRq citaRq
    )
            throws BadRequestException;
}
