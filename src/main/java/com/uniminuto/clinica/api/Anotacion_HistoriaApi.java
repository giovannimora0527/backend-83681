package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Anotacion_Historia;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.Anotacion_HistoriaRq;
import com.uniminuto.clinica.models.UsuarioRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST para la gestión de anotaciones médicas asociadas a una historia clínica.
 * Expone las operaciones CRUD básicas: crear, listar, filtrar por fecha y actualizar.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/anotacion-historia")
public interface Anotacion_HistoriaApi {

    /**
     * Lista todas las anotaciones de una historia médica.
     *
     * @param historiaId identificador de la historia a consultar
     * @return respuesta HTTP con la lista de anotaciones
     * @throws BadRequestException si la historia no existe o la petición es inválida
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Anotacion_Historia>> listarAnotaciones(@RequestParam Long historiaId) throws BadRequestException;

    /**
     * Lista las anotaciones de una historia dentro de un rango de fechas.
     *
     * @param historiaId identificador de la historia a consultar
     * @param fechaInicial fecha inicial del rango (yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss)
     * @param fechaFinal fecha final del rango (yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss)
     * @return respuesta HTTP con la colección filtrada por fecha
     * @throws BadRequestException si el parámetro es nulo, vacío o con formato inválido
     */
    @GetMapping(value = "/filtrar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Anotacion_Historia>> filtrarAnotaciones(
            @RequestParam Long historiaId,
            @RequestParam String fechaInicial,
            @RequestParam String fechaFinal) throws BadRequestException;

    /**
     * Crea una nueva anotación asociada a una historia médica.
     *
     * @param rq payload con la información de la anotación
     * @return respuesta HTTP con el estado de la creación
     * @throws BadRequestException si los datos enviados no cumplen la validación
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<UsuarioRS> guardarAnotacion(
            @RequestBody Anotacion_HistoriaRq rq)
            throws BadRequestException;

    /**
     * Actualiza una anotación existente.
     *
     * @param rq payload con los datos actualizados
     * @return respuesta HTTP con el estado de la actualización
     * @throws BadRequestException si la anotación no existe o los datos son inválidos
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<UsuarioRS> actualizarAnotacion(@RequestBody Anotacion_HistoriaRq rq) throws BadRequestException;
}
