package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;

import java.util.List;

public interface FormulaMedicaService {

    List<FormulaMedica> obtenerFormulasOrdenadas()
            throws BadRequestException;

}
