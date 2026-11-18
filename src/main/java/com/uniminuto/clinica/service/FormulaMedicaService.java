package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;

import java.util.List;

/** Define la consulta de fórmulas ordenadas por su fecha de registro. */
public interface FormulaMedicaService {

    List<FormulaMedica> listarFormulasOrdenadas(boolean asc);
}
