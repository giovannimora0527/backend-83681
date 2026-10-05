package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;

import java.util.List;

public interface MedicamentoService {
    List<Medicamento> listarMedicamentos();
    Medicamento guardarMedicamento(Medicamento medicamento) throws BadRequestException;
    Medicamento actualizarMedicamento(Medicamento medicamento) throws BadRequestException;
}
