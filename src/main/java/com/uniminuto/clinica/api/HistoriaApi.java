package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.UsuarioRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API REST para operaciones sobre historias médicas y sus anotaciones.
 * Define los endpoints expuestos por el controlador HistoriaApiController.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/historia")
public interface HistoriaApi {

    /**
     * Lista todas las historias en el sistema ordenadas por fecha (más recientes primero).
     * @return ResponseEntity con lista de HistoriaMedica
     * @throws BadRequestException en caso de error de validación
     */
    @GetMapping(value = "/listar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<List<HistoriaMedica>> listarHistorias() throws BadRequestException;

    /**
     * Filtra historias por rango de fechas. Las fechas deben estar en formato ISO o yyyy-MM-dd.
     * @param fechaInicial fecha inicial (yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss)
     * @param fechaFinal fecha final (yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss)
     * @return ResponseEntity con lista de HistoriaMedica en el rango
     * @throws BadRequestException si parámetros inválidos
     */
    @GetMapping(value = "/filtrar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<List<HistoriaMedica>> filtrarHistorias(@RequestParam String fechaInicial, @RequestParam String fechaFinal) throws BadRequestException;

    /**
     * Crea una nueva anotación asociada a una historia médica.
     * @param rq cuerpo de la petición con historiaId, medicoId y descripcion
     * @return ResponseEntity con UsuarioRS indicando resultado
     * @throws BadRequestException si datos inválidos
     */
    @PostMapping(value = "/anotacion/guardar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<UsuarioRS> guardarAnotacion(@RequestBody AnotacionHistoriaRq rq) throws BadRequestException;

    /**
     * Actualiza una anotación existente.
     * @param rq cuerpo de la petición con id y campos a actualizar
     * @return ResponseEntity con UsuarioRS indicando resultado
     * @throws BadRequestException si datos inválidos
     */
    @PostMapping(value = "/anotacion/actualizar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<UsuarioRS> actualizarAnotacion(@RequestBody AnotacionHistoriaRq rq) throws BadRequestException;

    /**
     * Lista anotaciones asociadas a una historia.
     * @param historiaId identificador de la historia
     * @return ResponseEntity con lista de AnotacionHistoria
     * @throws BadRequestException si datos inválidos
     */
    @GetMapping(value = "/anotacion/listar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<List<AnotacionHistoria>> listarAnotaciones(@RequestParam Long historiaId) throws BadRequestException;
}
