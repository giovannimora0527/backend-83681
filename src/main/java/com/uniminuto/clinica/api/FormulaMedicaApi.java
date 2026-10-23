package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.models.FormulaMedicaFiltroRq;
import com.uniminuto.clinica.models.FormulaMedicaResumenRs;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contrato REST para la gestión de fórmulas médicas.
 * Ruta base: {@code /clinica/v1/formula-medica}. No existe un endpoint DELETE:
 * las fórmulas se inactivan porque están ligadas a información clínica.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {

    /**
     * Lista las fórmulas (vista resumida) paginadas, filtradas y ordenadas.
     *
     * @param filtro filtros recibidos como query params.
     * @return página de fórmulas (200).
     */
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<PaginaRs<FormulaMedicaResumenRs>> listarFormulas(FormulaMedicaFiltroRq filtro);

    /**
     * Consulta el detalle completo de una fórmula.
     *
     * @param formulaId identificador de la fórmula.
     * @return fórmula (200) o 404 si no existe.
     */
    @GetMapping(value = "/{formulaId}", produces = {"application/json"})
    ResponseEntity<FormulaMedica> consultarFormula(@PathVariable("formulaId") Long formulaId);

    /**
     * Crea una fórmula médica.
     *
     * @param rq datos de la fórmula.
     * @return 201 con el id creado; 400, 404 o 409 si incumple una regla.
     */
    @PostMapping(produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearFormula(@RequestBody FormulaMedicaRq rq);

    /**
     * Edita una fórmula activa.
     *
     * @param formulaId identificador de la fórmula.
     * @param rq        nuevos datos.
     * @return 200; 409 si la fórmula está inactiva.
     */
    @PutMapping(value = "/{formulaId}", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> editarFormula(@PathVariable("formulaId") Long formulaId,
                                                @RequestBody FormulaMedicaRq rq);

    /**
     * Inactiva una fórmula (borrado lógico).
     *
     * @param formulaId identificador de la fórmula.
     * @return 200; 409 si ya estaba inactiva.
     */
    @PatchMapping(value = "/{formulaId}/inactivar", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> inactivarFormula(@PathVariable("formulaId") Long formulaId);
}
