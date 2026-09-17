package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;

import java.util.List;

/**
 * Contrato de los servicios de negocio de la entidad FormulaMedica.
 */
public interface FormulaMedicaService {

    /**
     * Lista las formulas medicas del inventario ordenadas por fecha de creacion,
     * de la mas reciente a la mas antigua.
     *
     * @return Lista de formulas medicas ordenadas descendentemente.
     */
    List<FormulaMedica> listarFormulasMedicas();
}
