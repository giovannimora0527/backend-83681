package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.CitaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
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
 * Endpoints REST de las citas médicas (/cita). GET consulta, POST crea, PUT edita y PATCH aplica
 * la operación explícita de reactivación.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cita")
public interface CitaApi {

    /**
     * Lista todas las citas, de la más reciente a la más antigua.
     *
     * @return 200 con las citas.
     */
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<CitaRs>> listar();

    /**
     * Consulta una cita.
     *
     * @param id identificador de la cita.
     * @return 200 con la cita, o 404 si no existe.
     */
    @GetMapping(value = "/{id:\\d+}", produces = {"application/json"})
    ResponseEntity<CitaRs> consultar(@PathVariable("id") Long id);

    /**
     * Lista las citas de una mascota.
     *
     * @param mascotaId identificador de la mascota.
     * @return 200 con las citas, o 404 si la mascota no existe.
     */
    @GetMapping(value = "/mascota/{mascotaId:\\d+}", produces = {"application/json"})
    ResponseEntity<List<CitaRs>> listarPorMascota(@PathVariable("mascotaId") Long mascotaId);

    /**
     * Crea una cita.
     *
     * @param rq datos de la cita.
     * @return 201 si se creó; 400 por datos inválidos; 409 por conflicto de horario.
     */
    @PostMapping(value = "/crear", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crear(@RequestBody CitaRq rq);

    /**
     * Edita una cita.
     *
     * @param id identificador de la cita.
     * @param rq datos nuevos.
     * @return 200 si se editó; 404, 400 o 409 según el caso.
     */
    @PutMapping(value = "/{id:\\d+}", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> editar(@PathVariable("id") Long id, @RequestBody CitaRq rq);

    /**
     * Reactiva una cita cancelada.
     *
     * @param id identificador de la cita.
     * @return 200 si se reactivó; 404 o 409 según el caso.
     */
    @PatchMapping(value = "/{id:\\d+}/reactivar", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> reactivar(@PathVariable("id") Long id);
}
