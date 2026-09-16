package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;

import java.util.List;

public interface FormulaMedicaService {

    /**
     * Lista todas las fórmulas médicas del inventario, ordenadas por su
     * fecha de creación, desde la más reciente hasta la más antigua.
     *
     * @return lista de fórmulas médicas ordenada de forma descendente.
     * @throws BadRequestException si ocurre un error de validación.
     */
    List<FormulaMedica> listarFormulasMedicas() throws BadRequestException;
}