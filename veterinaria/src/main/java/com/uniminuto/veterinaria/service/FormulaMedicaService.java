package com.uniminuto.veterinaria.service;

import com.uniminuto.veterinaria.entity.FormulaMedica;
import java.util.List;

/**
 * Interfaz de la capa de servicio para definir las operaciones de negocio del recurso FormulaMedica.
 */
public interface FormulaMedicaService {

    List<FormulaMedica> listarTodas();

    FormulaMedica guardarFormula(FormulaMedica formulaMedica);

    FormulaMedica actualizarFormula(Long id, FormulaMedica formulaMedica);
}