package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.HorarioMedicoRq;
import com.uniminuto.clinica.models.HorarioMedicoRs;
import com.uniminuto.clinica.models.CerrarConsultaRq;
import com.uniminuto.clinica.models.DisponibilidadRs;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/cita")
public interface CitaApi {

    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Cita>> listarCitas()
            throws BadRequestException;


    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/listar-rango",
            produces = {"application/json"})
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
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/listar-por-mascota",
            produces = {"application/json"})
    ResponseEntity<List<Cita>> listarCitasPorMascota(
            @RequestParam Integer mascotaId
    )
            throws BadRequestException;


    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @PostMapping(value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearCita(
            @RequestBody CitaRq cita
    )
            throws BadRequestException;


    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarCita(
            @RequestBody CitaRq cita
    )
            throws BadRequestException;

    @GetMapping(value = "/horarios", produces = {"application/json"})
    ResponseEntity<List<HorarioMedicoRs>> listarHorarios(@RequestParam Long medicoId);

    @GetMapping(value = "/disponibilidad", produces = {"application/json"})
    ResponseEntity<DisponibilidadRs> consultarDisponibilidad(@RequestParam Long medicoId,
            @RequestParam LocalDate fecha, @RequestParam(required = false) Integer duracionMinutos);

    @PostMapping(value = "/horarios", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<HorarioMedicoRs> guardarHorario(@RequestBody HorarioMedicoRq horario);

    @PostMapping(value = "/horarios/{id}/desactivar", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> desactivarHorario(@PathVariable("id") Long id);

    @PostMapping(value = "/{id}/cerrar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> cerrarConsulta(@PathVariable("id") Long id, @RequestBody CerrarConsultaRq request);
}
