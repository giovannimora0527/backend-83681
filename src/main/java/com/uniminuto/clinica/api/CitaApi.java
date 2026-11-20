package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.DisponibilidadMedicoRq;
import com.uniminuto.clinica.entity.CitaDisponibilidad;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
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

    @GetMapping(value = "/mis-citas", produces = {"application/json"})
    ResponseEntity<List<Cita>> listarMisCitas(@RequestParam LocalDateTime fechaInicio,
                                               @RequestParam LocalDateTime fechaFin,
                                               jakarta.servlet.http.HttpServletRequest request);


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

    @PostMapping(value = "/disponibilidad/crear", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<CitaDisponibilidad> crearDisponibilidad(@RequestBody DisponibilidadMedicoRq disponibilidad);

    @GetMapping(value = "/disponibilidad/medico", produces = {"application/json"})
    ResponseEntity<List<CitaDisponibilidad>> disponibilidadMedico(@RequestParam Long medicoId);
}
