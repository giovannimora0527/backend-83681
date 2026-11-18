package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaCalendarioRs;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.ReprogramarCitaRq;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Expone el calendario, las citas de una mascota y la gestión de citas. El calendario y su Excel
 * muestran la agenda del veterinario autenticado.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cita")
public interface CitaApi {

    /**
     * Consulta la agenda del veterinario por intervalo. El inicio se incluye y el final se
     * excluye.
     */
    @GetMapping(value = "/calendario", produces = "application/json")
    List<CitaCalendarioRs> calendario(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin);

    /**
     * Descarga la agenda del veterinario en Excel. Si se envían solo fechas, se incluye todo el
     * último día.
     */
    @GetMapping(value = "/calendario/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ResponseEntity<byte[]> excel(@RequestParam String inicio, @RequestParam String fin) throws IOException;

    /** Entrega los datos de una cita y comprueba la asignación si consulta un veterinario. */
    @GetMapping(value = "/{id}", produces = "application/json")
    CitaCalendarioRs detalle(@PathVariable Long id);

    /** Cambia la fecha y hora de una cita abierta manteniendo al paciente, al médico y la duración. */
    @PatchMapping(value = "/{id}/reprogramar", consumes = "application/json", produces = "application/json")
    CitaCalendarioRs reprogramar(@PathVariable Long id, @RequestBody ReprogramarCitaRq solicitud);

    /** Lista las citas visibles para la cuenta autenticada. */
    @GetMapping(value = "/listar", produces = "application/json")
    ResponseEntity<List<Cita>> listarCitas()
            throws BadRequestException;


    /** Lista las citas visibles que se cruzan con el intervalo solicitado. */
    @GetMapping(value = "/listar-rango", produces = "application/json")
    ResponseEntity<List<Cita>> listarCitasRango(
            @RequestParam LocalDateTime fechaInicio,
            @RequestParam LocalDateTime fechaFin
    )
            throws BadRequestException;


    /**
     * Lista las citas de una mascota.
     *
     * @param mascotaId id de la mascota.
     * @return citas de la mascota ordenadas por fecha descendente.
     * @throws BadRequestException cuando la mascota no existe.
     */
    @GetMapping(value = "/listar-por-mascota", produces = "application/json")
    ResponseEntity<List<Cita>> listarCitasPorMascota(
            @RequestParam Long mascotaId
    )
            throws BadRequestException;


    /** Programa una cita y valida el horario, la disponibilidad y los cruces. */
    @PostMapping(value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearCita(
            @RequestBody CitaRq cita
    )
            throws BadRequestException;


    /**
     * Edita una cita o registra su confirmación o cancelación. El cierre clínico se hace desde
     * Atención.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarCita(
            @RequestBody CitaRq cita
    )
            throws BadRequestException;
}
