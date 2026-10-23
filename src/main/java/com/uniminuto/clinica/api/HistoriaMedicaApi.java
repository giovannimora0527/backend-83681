package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.models.HistoriaMedicaFiltroRq;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
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
 * Contrato REST para la gestión de historias clínicas.
 * Ruta base: {@code /clinica/v1/historia-medica}.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/historia-medica")
public interface HistoriaMedicaApi {

    /**
     * Lista las historias clínicas paginadas, filtradas y ordenadas.
     *
     * @param filtro filtros recibidos como query params.
     * @return página de historias (200).
     */
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<PaginaRs<HistoriaMedica>> listarHistorias(HistoriaMedicaFiltroRq filtro);

    /**
     * Consulta una historia clínica por su id.
     *
     * @param historiaId identificador de la historia.
     * @return historia (200) o 404 si no existe.
     */
    @GetMapping(value = "/{historiaId}", produces = {"application/json"})
    ResponseEntity<HistoriaMedica> consultarHistoria(@PathVariable("historiaId") Long historiaId);

    /**
     * Crea una historia clínica a partir de una cita atendida.
     *
     * @param rq datos de la historia.
     * @return 201 con el id creado; 400, 404 o 409 si incumple una regla.
     */
    @PostMapping(produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearHistoria(@RequestBody HistoriaMedicaRq rq);

    /**
     * Cierra una historia clínica.
     *
     * @param historiaId identificador de la historia.
     * @return 200; 409 si ya estaba cerrada.
     */
    @PatchMapping(value = "/{historiaId}/cerrar", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> cerrarHistoria(@PathVariable("historiaId") Long historiaId);

    /**
     * Lista las historias clínicas de una mascota.
     *
     * @param mascotaId identificador de la mascota.
     * @return historias (200) o 404 si la mascota no existe.
     */
    @GetMapping(value = "/mascota/{mascotaId}", produces = {"application/json"})
    ResponseEntity<List<HistoriaMedica>> listarPorMascota(@PathVariable("mascotaId") Long mascotaId);
}
