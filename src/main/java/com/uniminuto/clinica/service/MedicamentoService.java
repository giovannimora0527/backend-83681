package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/**
 * Operaciones de negocio sobre el catálogo de medicamentos.
 */
public interface MedicamentoService {

    List<Medicamento> listarMedicamentos();

    Medicamento buscarMedicamentoPorNombre(String nombre) throws BadRequestException;

    MiRespuestaRS guardarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException;

    MiRespuestaRS actualizarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException;
}
