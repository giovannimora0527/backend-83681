package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

public interface MedicamentoService {

    List<Medicamento> listarMedicamentos() throws BadRequestException;

    List<Medicamento> buscarMedicamentos(String termino);

    MiRespuestaRS crearMedicamento(MedicamentoRq medicamentoRq);

    MiRespuestaRS actualizarMedicamento(MedicamentoRq medicamentoRq);
}
