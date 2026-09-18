package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;

import java.util.List;

/**
 * Servicio para la gestión de fórmulas médicas del inventario.
 */
public interface FormulaMedicaService {

    /**
     * Obtiene el listado de fórmulas médicas del inventario ordenadas por
     * fecha de creación, de la más reciente a la más antigua.
     *
     * @return lista de fórmulas médicas.
     * @throws BadRequestException excepción en caso de error de negocio.
     */
    List<FormulaMedica> listarFormulasMedicas() throws BadRequestException;
}
