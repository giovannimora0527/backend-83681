package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.FiltroPaginadoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contrato REST para las anotaciones de historias clínicas.
 * Ruta base: {@code /clinica/v1/anotacion-historia}.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/anotacion-historia")
public interface AnotacionHistoriaApi {

    /**
     * Agrega una anotación a una historia clínica.
     *
     * @param rq datos de la anotación.
     * @return 201 con el id creado; 400, 404 o 409 si incumple una regla.
     */
    @PostMapping(produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearAnotacion(@RequestBody AnotacionHistoriaRq rq);

    /**
     * Lista las anotaciones de una historia clínica.
     *
     * @param historiaId identificador de la historia.
     * @param filtro     filtros recibidos como query params.
     * @return página de anotaciones (200) o 404 si la historia no existe.
     */
    @GetMapping(value = "/historia/{historiaId}", produces = {"application/json"})
    ResponseEntity<PaginaRs<AnotacionHistoria>> listarPorHistoria(@PathVariable("historiaId") Long historiaId,
                                                                  FiltroPaginadoRq filtro);

    /**
     * Lista el histórico de anotaciones de una mascota.
     *
     * @param mascotaId identificador de la mascota.
     * @param filtro    filtros recibidos como query params.
     * @return página de anotaciones (200) o 404 si la mascota no existe.
     */
    @GetMapping(value = "/mascota/{mascotaId}", produces = {"application/json"})
    ResponseEntity<PaginaRs<AnotacionHistoria>> listarPorMascota(@PathVariable("mascotaId") Long mascotaId,
                                                                 FiltroPaginadoRq filtro);

    /**
     * Consulta el detalle de una anotación.
     *
     * @param anotacionId identificador de la anotación.
     * @return anotación (200) o 404 si no existe.
     */
    @GetMapping(value = "/{anotacionId}", produces = {"application/json"})
    ResponseEntity<AnotacionHistoria> consultarAnotacion(@PathVariable("anotacionId") Long anotacionId);
}
