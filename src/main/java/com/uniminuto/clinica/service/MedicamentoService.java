package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;

import java.util.List;

public interface MedicamentoService {

    List<Medicamento> listarMedicamentos(String busqueda, String tipoProducto) throws BadRequestException;

    Medicamento guardarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException;

    Medicamento actualizarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException;
}
