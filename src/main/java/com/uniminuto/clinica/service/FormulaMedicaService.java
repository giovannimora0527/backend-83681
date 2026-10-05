package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.UsuarioRS;

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

    UsuarioRS guardarFormulaMedica(FormulaMedica formula) throws BadRequestException;

    UsuarioRS actualizarFormulaMedica(FormulaMedica formula) throws BadRequestException;
}
