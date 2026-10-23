package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/**
 * Operaciones de negocio disponibles para las formulas medicas.
 */
public interface FormulaMedicaService {

    /**
     * Lista las formulas de la mas reciente a la mas antigua.
     *
     * @return lista de formulas.
     */
    List<FormulaMedica> listarFormulas();

    /**
     * Lista las formulas ordenadas por fecha de creacion.
     *
     * @param asc true ascendente, false descendente.
     * @return lista de formulas.
     */
    List<FormulaMedica> listarFormulasOrdenadas(boolean asc);

    /**
     * Consulta una formula por id.
     *
     * @param formulaId id de la formula.
     * @return formula encontrada.
     * @throws BadRequestException si no existe (404).
     */
    FormulaMedica consultarFormula(Long formulaId) throws BadRequestException;

    /**
     * Crea una formula aplicando las reglas de negocio.
     *
     * @param formulaRq datos de la formula.
     * @return respuesta de exito.
     * @throws BadRequestException si alguna regla no se cumple.
     */
    MiRespuestaRS crearFormula(FormulaMedicaRq formulaRq) throws BadRequestException;

    /**
     * Actualiza los datos editables de una formula activa.
     *
     * @param formulaRq datos de la formula (con id).
     * @return respuesta de exito.
     * @throws BadRequestException si alguna regla no se cumple.
     */
    MiRespuestaRS actualizarFormula(FormulaMedicaRq formulaRq) throws BadRequestException;

    /**
     * Inactiva una formula (no se borra fisicamente).
     *
     * @param formulaId id de la formula.
     * @return respuesta de exito.
     * @throws BadRequestException si ya estaba inactiva.
     */
    MiRespuestaRS inactivarFormula(Long formulaId) throws BadRequestException;
}
