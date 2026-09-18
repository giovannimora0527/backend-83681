package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
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
 * Contrato REST para la gestión de historias médicas de la Clínica
 * Veterinaria.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/historia-medica")
public interface HistoriaMedicaApi {

    /**
     * Crea una nueva historia médica en el sistema.
     *
     * @param historiaMedicaRq datos de la historia médica a crear.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException excepción en caso de error de negocio.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarHistoriaMedica(
            @RequestBody HistoriaMedicaRq historiaMedicaRq
    )
            throws BadRequestException;

    /**
     * Lista las historias médicas dada una fecha inicial y una fecha
     * final, organizadas de la más reciente a la más antigua.
     *
     * @param fechaInicial fecha inicial del rango a filtrar.
     * @param fechaFinal   fecha final del rango a filtrar.
     * @return lista de historias médicas encontradas.
     * @throws BadRequestException excepción en caso de error de negocio.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<HistoriaMedica>> listarHistoriasPorFecha(
            @RequestParam("fechaInicial") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicial,
            @RequestParam("fechaFinal") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFinal
    )
            throws BadRequestException;

    /**
     * Actualiza una historia médica almacenada en el sistema.
     *
     * @param historiaMedicaRq datos de la historia médica a actualizar.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException excepción en caso de error de negocio.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarHistoriaMedica(
            @RequestBody HistoriaMedicaRq historiaMedicaRq
    )
            throws BadRequestException;
}
