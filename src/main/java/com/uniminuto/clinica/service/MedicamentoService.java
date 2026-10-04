package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

public interface MedicamentoService {

    List<Medicamento> listar();

    MiRespuestaRS guardar(MedicamentoRq rq) throws BadRequestException;

    MiRespuestaRS actualizar(MedicamentoRq rq) throws BadRequestException;
}
