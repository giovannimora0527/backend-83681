package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
<<<<<<< HEAD
=======
import com.uniminuto.clinica.models.MiRespuestaRS;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661

import java.util.List;

/**
 * Operaciones de negocio sobre el catálogo de medicamentos.
 */
public interface MedicamentoService {

<<<<<<< HEAD
    List<Medicamento> listarMedicamentos(String busqueda, String tipoProducto) throws BadRequestException;

    Medicamento guardarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException;

    Medicamento actualizarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException;
=======
    List<Medicamento> listarMedicamentos();

    Medicamento buscarMedicamentoPorNombre(String nombre) throws BadRequestException;

    MiRespuestaRS guardarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException;

    MiRespuestaRS actualizarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
