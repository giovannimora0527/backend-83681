package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrato (endpoints) del modulo de formulas medicas.
 * Ruta base: /clinica/v1/formula-medica
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {

    /**
     * Lista las formulas, de la mas reciente a la mas antigua (fecha de creacion).
     *
     * @return lista de formulas.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<FormulaMedica>> listarFormulas() throws BadRequestException;

    /**
     * Lista las formulas ordenadas por fecha de creacion.
     *
     * @param asc true = mas antigua primero, false = mas reciente primero.
     * @return lista de formulas.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/listar-ordenado", produces = {"application/json"})
    ResponseEntity<List<FormulaMedica>> getFormulasByOrden(@RequestParam boolean asc)
            throws BadRequestException;

    /**
     * Consulta el detalle completo de una formula.
     *
     * @param formulaId id de la formula.
     * @return formula encontrada (404 si no existe).
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/consultar", produces = {"application/json"})
    ResponseEntity<FormulaMedica> consultarFormula(@RequestParam Long formulaId) throws BadRequestException;

    /**
     * Crea una formula medica. Responde 201.
     *
     * @param formulaRq datos de la formula.
     * @return mensaje de exito.
     * @throws BadRequestException si alguna regla no se cumple.
     */
    @PostMapping(value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearFormula(@RequestBody FormulaMedicaRq formulaRq)
            throws BadRequestException;

    /**
     * Actualiza los datos editables de una formula activa.
     *
     * @param formulaRq datos de la formula (con id).
     * @return mensaje de exito.
     * @throws BadRequestException si alguna regla no se cumple.
     */
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarFormula(@RequestBody FormulaMedicaRq formulaRq)
            throws BadRequestException;

    /**
     * Inactiva una formula (en lugar de borrarla fisicamente).
     *
     * @param formulaId id de la formula.
     * @return mensaje de exito.
     * @throws BadRequestException si ya estaba inactiva.
     */
    @PatchMapping(value = "/inactivar", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> inactivarFormula(@RequestParam Long formulaId) throws BadRequestException;
}
