package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.DespacharFormulaRq;
import com.uniminuto.clinica.models.DespachoRs;

import java.util.List;

public interface FormulaService {

    List<FormulaMedica> findAllFormulas();

    MiRespuestaRS guardarFormula(FormulaRq formulaRq) throws BadRequestException;

    MiRespuestaRS actualizarFormula(FormulaRq formulaRq) throws BadRequestException;

    MiRespuestaRS dispensar(Long formulaId) throws BadRequestException;

    DespachoRs dispensarParcial(Long formulaId, DespacharFormulaRq request) throws BadRequestException;

    byte[] generarPdf(Long formulaId);
}
