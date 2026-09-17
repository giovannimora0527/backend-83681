package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;

import java.util.List;

/**
 * Interfaz de servicio para la gestión de fórmulas médicas.
 * Define los métodos que deben implementarse para interactuar con las fórmulas médicas.
 */
public interface FormulaMedicaService {

    /**
     * Lista todas las fórmulas médicas registradas en el sistema.
     *
     * @return Lista de todas las fórmulas médicas.
     */
    List<FormulaMedica> listarFormulasMedicas();
}
