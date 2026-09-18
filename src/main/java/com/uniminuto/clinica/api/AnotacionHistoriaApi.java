package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/anotacion-historia")
public interface AnotacionHistoriaApi {

    /**
     * Registra una nueva anotación dentro de una historia médica.
     *
     * @param anotacionHistoriaRq datos de la anotación a crear.
     * @return mensaje de confirmación.
     * @throws BadRequestException excepcion.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarAnotacion(
            @RequestBody AnotacionHistoriaRq anotacionHistoriaRq)
            throws BadRequestException;

    /**
     * Lista las anotaciones de historia dado un rango de fechas.
     *
     * @param fechaInicio fecha y hora inicial del rango, formato ISO (ej: 2024-06-01T00:00:00).
     * @param fechaFin    fecha y hora final del rango, formato ISO (ej: 2024-06-30T23:59:59).
     * @return lista de anotaciones dentro del rango, de la más reciente a la más antigua.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<AnotacionHistoria>> getAnotaciones(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin)
            throws BadRequestException;

    /**
     * Actualiza una anotación de historia ya almacenada en el sistema.
     *
     * @param anotacionHistoriaRq datos nuevos de la anotación, incluyendo su id.
     * @return mensaje de confirmación.
     * @throws BadRequestException excepcion.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarAnotacion(
            @RequestBody AnotacionHistoriaRq anotacionHistoriaRq)
            throws BadRequestException;
}