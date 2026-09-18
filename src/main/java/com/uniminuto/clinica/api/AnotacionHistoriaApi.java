package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Contrato REST para la gestión de anotaciones de historia médica de la
 * Clínica Veterinaria.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/anotacion-historia")
public interface AnotacionHistoriaApi {

    /**
     * Crea una nueva anotación de historia médica.
     *
     * @param anotacionHistoriaRq datos de la anotación a crear.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException excepción en caso de error de negocio.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarAnotacion(
            @RequestBody AnotacionHistoriaRq anotacionHistoriaRq
    )
            throws BadRequestException;

    /**
     * Lista las anotaciones asociadas a una historia médica, ordenadas
     * de la más reciente a la más antigua.
     *
     * @param historiaId identificador de la historia médica.
     * @return lista de anotaciones de la historia médica.
     * @throws BadRequestException excepción en caso de error de negocio.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<AnotacionHistoria>> listarAnotacionesPorHistoria(
            @RequestParam("historiaId") Long historiaId
    )
            throws BadRequestException;

    /**
     * Actualiza una anotación de historia médica almacenada en el
     * sistema.
     *
     * @param anotacionHistoriaRq datos de la anotación a actualizar.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException excepción en caso de error de negocio.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarAnotacion(
            @RequestBody AnotacionHistoriaRq anotacionHistoriaRq
    )
            throws BadRequestException;
}
