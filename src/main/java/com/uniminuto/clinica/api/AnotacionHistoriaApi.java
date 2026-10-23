package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrato (endpoints) de las anotaciones de historia clinica.
 * Ruta base: /clinica/v1/anotacion-historia
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/anotacion-historia")
public interface AnotacionHistoriaApi {

    /**
     * Agrega una anotacion a una historia clinica existente. Responde 201.
     *
     * @param anotacionRq datos de la anotacion.
     * @return mensaje de exito.
     * @throws BadRequestException si alguna regla no se cumple.
     */
    @PostMapping(value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarAnotacion(@RequestBody AnotacionRq anotacionRq)
            throws BadRequestException;

    /**
     * Consulta las anotaciones de una historia clinica (de la mas reciente a la mas antigua).
     *
     * @param historiaId id de la historia.
     * @return anotaciones de la historia.
     * @throws BadRequestException si la historia no existe (404).
     */
    @GetMapping(value = "/por-historia", produces = {"application/json"})
    ResponseEntity<List<AnotacionHistoria>> listarPorHistoria(@RequestParam Long historiaId)
            throws BadRequestException;

    /**
     * Consulta el historial completo de anotaciones de una mascota
     * (todas sus historias), de la mas reciente a la mas antigua.
     *
     * @param mascotaId id de la mascota.
     * @return anotaciones de la mascota.
     * @throws BadRequestException si la mascota no existe (404).
     */
    @GetMapping(value = "/por-mascota", produces = {"application/json"})
    ResponseEntity<List<AnotacionHistoria>> listarPorMascota(@RequestParam Long mascotaId)
            throws BadRequestException;
}
