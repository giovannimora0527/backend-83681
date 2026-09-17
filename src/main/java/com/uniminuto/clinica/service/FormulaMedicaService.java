package com.uniminuto.clinica.service;

import java.util.List;

import com.uniminuto.clinica.entity.FormulaMedica;

/**
 * Contrato de servicio para la gestion de formulas medicas.
 */
public interface FormulaMedicaService {

    /**
     * Obtiene el listado de formulas medicas ordenado de la mas
     * reciente a la mas antigua segun su fecha de creacion.
     *
     * @return lista de formulas medicas.
     */
    List<FormulaMedica> listar();
}