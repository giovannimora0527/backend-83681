package com.uniminuto.clinica.api;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import com.uniminuto.clinica.models.Filtros;
import com.uniminuto.clinica.models.PaginaRS;
import com.uniminuto.clinica.models.AnotacionVistaRS;
import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrato REST del recurso anotación de historia clínica.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/anotacion-historia")
public interface AnotacionHistoriaApi {

    /**
     * Lista las anotaciones de una historia, de la más reciente a la más antigua.
     *
     * @param historiaId identificador de la historia.
     * @return anotaciones de la historia.
     * @throws BadRequestException si la historia no existe.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<AnotacionHistoria>> listarAnotaciones(
            @RequestParam Long historiaId)
            throws BadRequestException;

    /**
     * Filtra las anotaciones de una historia por rango de fechas.
     *
     * @param historiaId   identificador de la historia.
     * @param fechaInicial fecha inicial (yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss).
     * @param fechaFinal   fecha final.
     * @return anotaciones del rango.
     * @throws BadRequestException si los parámetros no son válidos.
     */
    @GetMapping(value = "/filtrar",
            produces = {"application/json"})
    ResponseEntity<List<AnotacionHistoria>> filtrarAnotaciones(
            @RequestParam Long historiaId,
            @RequestParam String fechaInicial,
            @RequestParam String fechaFinal)
            throws BadRequestException;

    /**
     * Consulta una anotación por su identificador.
     *
     * @param id identificador de la anotación.
     * @return anotación encontrada.
     * @throws BadRequestException si no existe.
     */
    @GetMapping(value = "/consultar",
            produces = {"application/json"})
    ResponseEntity<AnotacionHistoria> consultarAnotacion(
            @RequestParam Long id)
            throws BadRequestException;

    /**
     * Agrega una anotación a una historia existente.
     *
     * @param anotacionRq datos de la anotación.
     * @return resultado de la operación.
     * @throws BadRequestException si no cumple las reglas de negocio.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarAnotacion(
            @RequestBody AnotacionHistoriaRq anotacionRq)
            throws BadRequestException;

    /**
     * Actualiza el contenido de una anotación. La historia y la fecha no cambian.
     *
     * @param anotacionRq datos actualizados.
     * @return resultado de la operación.
     * @throws BadRequestException si no cumple las reglas de negocio.
     */
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarAnotacion(
            @RequestBody AnotacionHistoriaRq anotacionRq)
            throws BadRequestException;

    /**
     * Histórico paginado de anotaciones de una historia (DataTable del detalle).
     *
     * @return página de anotaciones.
     * @throws BadRequestException si la historia no existe o la paginación no es válida.
     */
    @GetMapping(value = "/paginado", produces = {"application/json"})
    ResponseEntity<PaginaRS<AnotacionVistaRS>> listarAnotacionesPaginadas(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio,
            @RequestParam(required = false) String campo,
            @RequestParam(required = false) String direccion,
            @RequestParam Long historiaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String texto)
            throws BadRequestException;
}
