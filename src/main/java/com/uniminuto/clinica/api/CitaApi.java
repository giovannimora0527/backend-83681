package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.models.CambioEstadoCitaRq;
import com.uniminuto.clinica.models.CitaFiltroRq;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import com.uniminuto.clinica.models.ReactivarCitaRq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Contrato REST para la gestión de citas médicas.
 * Ruta base: {@code /clinica/v1/cita}.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cita")
public interface CitaApi {

    /**
     * Lista las citas paginadas, filtradas y ordenadas.
     * Ejemplo: {@code GET /cita/listar?pagina=0&tamanio=10&ordenarPor=fecha&direccion=desc&estado=PENDIENTE}.
     *
     * @param filtro filtros recibidos como query params.
     * @return página de citas (200).
     */
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<PaginaRs<Cita>> listarCitas(CitaFiltroRq filtro);

    /**
     * Consulta una cita por su id.
     *
     * @param citaId identificador de la cita.
     * @return cita (200) o 404 si no existe.
     */
    @GetMapping(value = "/{citaId}", produces = {"application/json"})
    ResponseEntity<Cita> consultarCita(@PathVariable("citaId") Long citaId);

    /**
     * Crea una cita.
     *
     * @param citaRq datos de la cita.
     * @return 201 con el id creado; 400, 404 o 409 si incumple una regla.
     */
    @PostMapping(produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearCita(@RequestBody CitaRq citaRq);

    /**
     * Edita una cita activa (fecha, hora, veterinario y motivo).
     *
     * @param citaId identificador de la cita.
     * @param citaRq nuevos datos.
     * @return 200; 409 si la cita está cancelada o atendida.
     */
    @PutMapping(value = "/{citaId}", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> editarCita(@PathVariable("citaId") Long citaId, @RequestBody CitaRq citaRq);

    /**
     * Cambia el estado de una cita (confirmar, atender, cancelar...).
     *
     * @param citaId identificador de la cita.
     * @param rq     nuevo estado.
     * @return 200; 409 si la transición no está permitida.
     */
    @PatchMapping(value = "/{citaId}/estado", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> cambiarEstado(@PathVariable("citaId") Long citaId,
                                                @RequestBody CambioEstadoCitaRq rq);

    /**
     * Reactiva una cita cancelada (operación explícita).
     *
     * @param citaId identificador de la cita.
     * @param rq     nueva fecha opcional.
     * @return 200; 409 si la cita no está cancelada o no hay disponibilidad.
     */
    @PatchMapping(value = "/{citaId}/reactivar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> reactivarCita(@PathVariable("citaId") Long citaId,
                                                @RequestBody(required = false) ReactivarCitaRq rq);

    /**
     * Lista las citas atendidas de una mascota que aún no tienen historia clínica.
     *
     * @param mascotaId identificador de la mascota.
     * @return citas disponibles (200) o 404 si la mascota no existe.
     */
    @GetMapping(value = "/mascota/{mascotaId}/atendidas-sin-historia", produces = {"application/json"})
    ResponseEntity<List<Cita>> listarAtendidasSinHistoria(@PathVariable("mascotaId") Long mascotaId);
}
