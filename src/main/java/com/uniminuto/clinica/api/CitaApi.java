package com.uniminuto.clinica.api;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import com.uniminuto.clinica.models.Filtros;
import com.uniminuto.clinica.models.PaginaRS;
import com.uniminuto.clinica.models.CitaVistaRS;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrato REST del recurso cita.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cita")
public interface CitaApi {

    /**
     * Lista todas las citas, de la más reciente a la más antigua.
     *
     * @return lista de citas.
     * @throws BadRequestException si ocurre un error de negocio.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Cita>> listarCitas()
            throws BadRequestException;

    /**
     * Filtra citas por rango de fechas (yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss).
     *
     * @param fechaInicial fecha inicial.
     * @param fechaFinal   fecha final.
     * @return citas dentro del rango.
     * @throws BadRequestException si las fechas no son válidas.
     */
    @GetMapping(value = "/filtrar",
            produces = {"application/json"})
    ResponseEntity<List<Cita>> filtrarCitas(
            @RequestParam String fechaInicial,
            @RequestParam String fechaFinal)
            throws BadRequestException;

    /**
     * Consulta una cita por su identificador.
     *
     * @param id identificador de la cita.
     * @return cita encontrada.
     * @throws BadRequestException si no existe.
     */
    @GetMapping(value = "/consultar",
            produces = {"application/json"})
    ResponseEntity<Cita> consultarCita(
            @RequestParam Long id)
            throws BadRequestException;

    /**
     * Registra una nueva cita validando cliente, mascota, médico, horarios y estado.
     *
     * @param citaRq datos de la cita.
     * @return resultado de la operación.
     * @throws BadRequestException si no cumple las reglas de negocio.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarCita(
            @RequestBody CitaRq citaRq)
            throws BadRequestException;

    /**
     * Actualiza una cita según su estado. Las citas canceladas o atendidas no se modifican.
     *
     * @param citaRq datos actualizados.
     * @return resultado de la operación.
     * @throws BadRequestException si no cumple las reglas de negocio.
     */
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarCita(
            @RequestBody CitaRq citaRq)
            throws BadRequestException;

    /**
     * Reactiva una cita cancelada. Es una operación explícita, separada de la edición.
     *
     * @param id identificador de la cita.
     * @return resultado de la operación.
     * @throws BadRequestException si la cita no existe, no está cancelada o hay choque de horario.
     */
    @PostMapping(value = "/reactivar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> reactivarCita(
            @RequestParam Long id)
            throws BadRequestException;

    /**
     * Listado paginado y filtrable de citas (DataTable).
     *
     * @return página de citas en vista resumida.
     * @throws BadRequestException si la paginación o los filtros no son válidos.
     */
    @GetMapping(value = "/paginado", produces = {"application/json"})
    ResponseEntity<PaginaRS<CitaVistaRS>> listarCitasPaginadas(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio,
            @RequestParam(required = false) String campo,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Integer mascotaId,
            @RequestParam(required = false) Long medicoId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String texto)
            throws BadRequestException;
}
