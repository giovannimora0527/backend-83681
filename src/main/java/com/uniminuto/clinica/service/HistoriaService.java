package com.uniminuto.clinica.service;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.HistoriaMedicaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/** Define la consulta y actualización del historial clínico asociado a las citas. */
public interface HistoriaService {

    /** Consulta las historias clínicas registradas. */
    List<HistoriaMedicaRs> getAllHistorias();

    /** Busca las historias clínicas que coinciden con el criterio. */
    List<HistoriaMedicaRs> buscarHistorias(String termino);

    /** Guarda los datos de la historia clínica. */
    MiRespuestaRS guardarHistoria(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException;

    /** Guarda cambios en la historia clínica del paciente. */
    MiRespuestaRS actualizarHistoria(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException;
}
