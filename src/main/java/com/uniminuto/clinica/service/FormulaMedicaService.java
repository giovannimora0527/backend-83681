package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;
import java.util.List;

/**
 * Interfaz de servicio para la gestión de fórmulas médicas.
 */
public interface FormulaMedicaService {

    /**
     * Obtiene las fórmulas médicas ordenadas por fecha de creación desc.
     * 
     * @return Lista de fórmulas médicas.
     */
    List<FormulaMedica> obtenerFormulasOrdenadas();

    /**
     * Guarda una nueva fórmula médica en el sistema.
     * 
     * @param formulaMedica Entidad de la fórmula médica a guardar.
     * @return La fórmula médica guardada.
     */
    FormulaMedica guardarFormula(FormulaMedica formulaMedica);
}