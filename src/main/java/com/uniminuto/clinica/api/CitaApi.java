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
<<<<<<< HEAD
import com.uniminuto.clinica.models.HorarioMedicoRq;
import com.uniminuto.clinica.models.HorarioMedicoRs;
import com.uniminuto.clinica.models.CerrarConsultaRq;
import com.uniminuto.clinica.models.DisponibilidadRs;
import java.time.LocalDate;
=======
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

<<<<<<< HEAD
=======
/**
 * Contrato REST del recurso cita.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
@RequestMapping("/cita")
public interface CitaApi {

    /**
<<<<<<< HEAD
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
=======
     * Lista todas las citas, de la más reciente a la más antigua.
     *
     * @return lista de citas.
     * @throws BadRequestException si ocurre un error de negocio.
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Cita>> listarCitas()
            throws BadRequestException;
<<<<<<< HEAD


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
=======

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
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
