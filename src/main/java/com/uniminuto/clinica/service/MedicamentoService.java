package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;
import java.util.Map;

public interface MedicamentoService {

    List<Medicamento> listarMedicamentos() throws BadRequestException;

    List<Medicamento> consultarDisponiblesEnInventario() throws BadRequestException;

    MiRespuestaRS registrarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException;

    MiRespuestaRS editarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException;

    Map<String, Object> consultarEstadoInventario() throws BadRequestException;
}
