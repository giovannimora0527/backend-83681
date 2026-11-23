package com.uniminuto.clinica.api;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import com.uniminuto.clinica.models.Filtros;
import com.uniminuto.clinica.models.PaginaRS;
import com.uniminuto.clinica.models.HistoriaVistaRS;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrato REST del recurso historia clínica.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/historia")
public interface HistoriaMedicaApi {

    /**
     * Lista todas las historias clínicas, de la más reciente a la más antigua.
     *
     * @return lista de historias.
     * @throws BadRequestException si ocurre un error de negocio.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<HistoriaMedica>> listarHistorias()
            throws BadRequestException;

    /**
     * Filtra historias por fecha de consulta (yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss).
     *
     * @param fechaInicial fecha inicial.
     * @param fechaFinal   fecha final.
     * @return historias del rango.
     * @throws BadRequestException si las fechas no son válidas.
     */
    @GetMapping(value = "/filtrar",
            produces = {"application/json"})
    ResponseEntity<List<HistoriaMedica>> filtrarHistorias(
            @RequestParam String fechaInicial,
            @RequestParam String fechaFinal)
            throws BadRequestException;

    /**
     * Lista las historias de una mascota.
     *
     * @param mascotaId identificador de la mascota.
     * @return historias de la mascota.
     * @throws BadRequestException si la mascota no existe.
     */
    @GetMapping(value = "/listar-por-mascota",
            produces = {"application/json"})
    ResponseEntity<List<HistoriaMedica>> listarHistoriasPorMascota(
            @RequestParam Integer mascotaId)
            throws BadRequestException;

    /**
     * Consulta una historia clínica por su identificador.
     *
     * @param id identificador de la historia.
     * @return historia encontrada.
     * @throws BadRequestException si no existe.
     */
    @GetMapping(value = "/consultar",
            produces = {"application/json"})
    ResponseEntity<HistoriaMedica> consultarHistoria(
            @RequestParam Long id)
            throws BadRequestException;

    /**
     * Crea una historia clínica a partir de una cita atendida.
     *
     * @param historiaRq datos de la historia.
     * @return resultado de la operación.
     * @throws BadRequestException si no cumple las reglas de negocio.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarHistoria(
            @RequestBody HistoriaMedicaRq historiaRq)
            throws BadRequestException;

    /**
     * Actualiza motivo, diagnóstico y observaciones de una historia clínica.
     *
     * @param historiaRq datos actualizados.
     * @return resultado de la operación.
     * @throws BadRequestException si no cumple las reglas de negocio.
     */
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarHistoria(
            @RequestBody HistoriaMedicaRq historiaRq)
            throws BadRequestException;

    /**
     * Listado paginado y filtrable de historias clínicas (DataTable).
     *
     * @return página de historias en vista resumida.
     * @throws BadRequestException si la paginación o los filtros no son válidos.
     */
    @GetMapping(value = "/paginado", produces = {"application/json"})
    ResponseEntity<PaginaRS<HistoriaVistaRS>> listarHistoriasPaginadas(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio,
            @RequestParam(required = false) String campo,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Integer mascotaId,
            @RequestParam(required = false) Long medicoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String texto)
            throws BadRequestException;
}
