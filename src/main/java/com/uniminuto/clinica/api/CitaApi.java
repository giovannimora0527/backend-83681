package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.CitaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRequest;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato (endpoints) del modulo de citas medicas.
 * Ruta base: /clinica/v1/cita
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cita")
public interface CitaApi {

    /**
     * Lista todas las citas, de la mas reciente a la mas antigua.
     *
     * @return lista de citas.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<CitaMedica>> listarCitas() throws BadRequestException;

    /**
     * Consulta una cita por su id.
     *
     * @param citaId id de la cita.
     * @return cita encontrada (404 si no existe).
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/consultar", produces = {"application/json"})
    ResponseEntity<CitaMedica> consultarCita(@RequestParam Long citaId) throws BadRequestException;

    /**
     * Lista las citas de una mascota (para los combos de historia y formula).
     *
     * @param mascotaId id de la mascota.
     * @return citas de la mascota.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/por-mascota", produces = {"application/json"})
    ResponseEntity<List<CitaMedica>> listarCitasPorMascota(@RequestParam Long mascotaId)
            throws BadRequestException;

    /**
     * Lista las citas entre dos fechas.
     * Formato de fecha: yyyy-MM-ddTHH:mm:ss (ej: 2026-10-01T00:00:00).
     *
     * @param fechaInicio fecha inicial.
     * @param fechaFinal fecha final.
     * @return citas en el rango.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/filtrar-by-fechas", produces = {"application/json"})
    ResponseEntity<List<CitaMedica>> filtrarCitasPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFinal)
            throws BadRequestException;

    /**
     * Crea una cita nueva. Responde 201 (Created) si todo es correcto.
     *
     * @param citaRq datos de la cita.
     * @return mensaje de exito.
     * @throws BadRequestException si alguna regla de negocio no se cumple.
     */
    @PostMapping(value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearCita(@RequestBody CitaRequest citaRq) throws BadRequestException;

    /**
     * Actualiza una cita existente teniendo en cuenta su estado.
     *
     * @param citaRq datos de la cita (debe incluir el id).
     * @return mensaje de exito.
     * @throws BadRequestException si alguna regla de negocio no se cumple.
     */
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarCita(@RequestBody CitaRequest citaRq) throws BadRequestException;

    /**
     * Operacion explicita para reactivar una cita CANCELADA (pasa a PENDIENTE).
     * Se usa PATCH porque solo cambia una parte de la cita (el estado).
     *
     * @param citaId id de la cita cancelada.
     * @return mensaje de exito.
     * @throws BadRequestException si la cita no se puede reactivar.
     */
    @PatchMapping(value = "/reactivar", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> reactivarCita(@RequestParam Long citaId) throws BadRequestException;
}
