package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.DispensacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

public interface DispensacionService {

    MiRespuestaRS dispensarMedicamento(DispensacionRq dispensacionRq) throws BadRequestException;

    List<MovimientoInventario> listarHistorialPorMedicamento(Long medicamentoId) throws BadRequestException;

    List<MovimientoInventario> listarHistorialPorFormula(Long formulaId) throws BadRequestException;
}