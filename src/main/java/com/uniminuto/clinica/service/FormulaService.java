package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PrescripcionRq;

import java.util.List;

/** Define cómo consultar, emitir y editar fórmulas, y revisar la disponibilidad de medicamentos. */
public interface FormulaService {

    /** Registra una fórmula con uno o varios medicamentos para una cita abierta. */
    long formular(long citaId, PrescripcionRq solicitud);

    /** Devuelve las unidades disponibles según el stock y los lotes vigentes. */
    long existencias(long medicamentoId);

    List<FormulaMedica> findAllFormulas();

    List<FormulaMedica> buscarFormulas(String termino);

    /** Registra la fórmula desde el formulario individual. */
    MiRespuestaRS guardarFormula(FormulaRq formulaRq) throws BadRequestException;

    /** Actualiza una fórmula individual vigente mientras no tenga entregas registradas. */
    MiRespuestaRS actualizarFormula(FormulaRq formulaRq) throws BadRequestException;
}
