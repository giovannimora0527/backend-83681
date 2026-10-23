package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.AnotacionRq;
import com.uniminuto.clinica.models.AnotacionRs;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.HistoriaMedicaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Endpoints REST de las historias clínicas y sus anotaciones (/historia-medica).
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/historia-medica")
public interface HistoriaMedicaApi {

    /**
     * Lista las historias de la consulta más reciente a la más antigua.
     *
     * @return 200 con las historias.
     */
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<HistoriaMedicaRs>> listar();

    /**
     * Consulta una historia clínica.
     *
     * @param id identificador de la historia.
     * @return 200 con la historia, o 404 si no existe.
     */
    @GetMapping(value = "/{id:\\d+}", produces = {"application/json"})
    ResponseEntity<HistoriaMedicaRs> consultar(@PathVariable("id") Long id);

    /**
     * Lista las historias de una mascota.
     *
     * @param mascotaId identificador de la mascota.
     * @return 200 con las historias, o 404 si la mascota no existe.
     */
    @GetMapping(value = "/mascota/{mascotaId:\\d+}", produces = {"application/json"})
    ResponseEntity<List<HistoriaMedicaRs>> listarPorMascota(@PathVariable("mascotaId") Long mascotaId);

    /**
     * Crea una historia clínica.
     *
     * @param rq datos de la historia.
     * @return 201 si se creó; 400 por datos inválidos; 409 si la cita no puede generar historia.
     */
    @PostMapping(value = "/crear", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crear(@RequestBody HistoriaMedicaRq rq);

    /**
     * Cierra una historia clínica.
     *
     * @param id identificador de la historia.
     * @return 200 si se cerró; 404 o 409 según el caso.
     */
    @PatchMapping(value = "/{id:\\d+}/cerrar", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> cerrar(@PathVariable("id") Long id);

    /**
     * Lista las anotaciones de una historia, de la más reciente a la más antigua.
     *
     * @param id identificador de la historia.
     * @return 200 con las anotaciones, o 404 si la historia no existe.
     */
    @GetMapping(value = "/{id:\\d+}/anotaciones", produces = {"application/json"})
    ResponseEntity<List<AnotacionRs>> listarAnotaciones(@PathVariable("id") Long id);

    /**
     * Consulta el detalle de una anotación.
     *
     * @param anotacionId identificador de la anotación.
     * @return 200 con la anotación, o 404 si no existe.
     */
    @GetMapping(value = "/anotaciones/{anotacionId:\\d+}", produces = {"application/json"})
    ResponseEntity<AnotacionRs> consultarAnotacion(@PathVariable("anotacionId") Long anotacionId);

    /**
     * Agrega una anotación a una historia.
     *
     * @param id identificador de la historia.
     * @param rq datos de la anotación.
     * @return 201 si se creó; 400, 404 o 409 según el caso.
     */
    @PostMapping(value = "/{id:\\d+}/anotaciones", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearAnotacion(@PathVariable("id") Long id, @RequestBody AnotacionRq rq);
}
