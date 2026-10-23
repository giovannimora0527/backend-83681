package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.FormulaDetalleRs;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.FormulaResumenRs;
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
 * Endpoints REST de las fórmulas médicas (/formula-medica).
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {

    /**
     * Lista las fórmulas (resumen) de la más reciente a la más antigua por fecha de creación.
     *
     * @return 200 con las fórmulas resumidas.
     */
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<FormulaResumenRs>> listar();

    /**
     * Consulta el detalle completo de una fórmula.
     *
     * @param id identificador de la fórmula.
     * @return 200 con la fórmula, o 404 si no existe.
     */
    @GetMapping(value = "/{id:\\d+}", produces = {"application/json"})
    ResponseEntity<FormulaDetalleRs> consultar(@PathVariable("id") Long id);

    /**
     * Crea una fórmula médica.
     *
     * @param rq datos de la fórmula.
     * @return 201 si se creó; 400 por datos o reglas incumplidas.
     */
    @PostMapping(value = "/crear", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crear(@RequestBody FormulaMedicaRq rq);

    /**
     * Edita una fórmula activa.
     *
     * @param id identificador de la fórmula.
     * @param rq datos nuevos.
     * @return 200 si se editó; 400, 404 o 409 según el caso.
     */
    @PutMapping(value = "/{id:\\d+}", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> editar(@PathVariable("id") Long id, @RequestBody FormulaMedicaRq rq);

    /**
     * Inactiva una fórmula (no se elimina físicamente).
     *
     * @param id identificador de la fórmula.
     * @return 200 si se inactivó; 404 o 409 según el caso.
     */
    @PatchMapping(value = "/{id:\\d+}/inactivar", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> inactivar(@PathVariable("id") Long id);
}
