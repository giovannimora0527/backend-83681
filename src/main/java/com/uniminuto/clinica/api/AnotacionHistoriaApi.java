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
 * Contrato REST de los servicios expuestos para la entidad AnotacionHistoria.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/anotacion-historia")
public interface AnotacionHistoriaApi {

    /**
     * Lista las anotaciones de historia, opcionalmente filtradas por la
     * historia medica a la que pertenecen.
     *
     * @param historiaId Identificador de la historia medica, opcional.
     * @return Lista de anotaciones encontradas.
     * @throws BadRequestException Excepcion de negocio.
     */
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<AnotacionHistoria>> listarAnotaciones(
            @RequestParam(required = false) Long historiaId)
            throws BadRequestException;

    /**
     * Crea una nueva anotacion de historia.
     *
     * @param anotacionHistoriaRq Datos de la anotacion a crear.
     * @return Respuesta con el estado de la operacion.
     * @throws BadRequestException Excepcion de negocio.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarAnotacion(
            @RequestBody AnotacionHistoriaRq anotacionHistoriaRq)
            throws BadRequestException;

    /**
     * Actualiza una anotacion de historia existente.
     *
     * @param anotacionHistoriaRq Datos de la anotacion a actualizar.
     * @return Respuesta con el estado de la operacion.
     * @throws BadRequestException Excepcion de negocio.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarAnotacion(
            @RequestBody AnotacionHistoriaRq anotacionHistoriaRq)
            throws BadRequestException;
}
