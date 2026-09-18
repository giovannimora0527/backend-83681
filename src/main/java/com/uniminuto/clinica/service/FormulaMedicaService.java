package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;

import java.util.List;

public interface FormulaMedicaService {

    /**
     * Lista todas las fórmulas médicas del inventario, de la más reciente a la más antigua.
     *
     * @return lista de fórmulas médicas ordenada por fecha de creación descendente.
     * @throws BadRequestException excepcion.
     */
    List<FormulaMedica> getListarFormulasMedicas() throws BadRequestException;
}