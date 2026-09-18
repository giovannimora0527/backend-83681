package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

public interface HistoriaMedicaService {

    /**
     * Lista todas las historias médicas, de la más reciente a la más antigua.
     *
     * @return lista de historias médicas ordenada por fecha de creación descendente.
     * @throws BadRequestException excepcion.
     */
    List<HistoriaMedica> listarHistoriasMedicas() throws BadRequestException;

    /**
     * Crea una nueva historia médica para un paciente.
     *
     * @param historiaMedicaRq datos de la historia médica a crear.
     * @return mensaje de confirmación.
     * @throws BadRequestException si los datos son inválidos o el paciente no existe.
     */
    MiRespuestaRS guardarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException;

    /**
     * Actualiza una historia médica ya existente.
     *
     * @param historiaMedicaRq datos nuevos de la historia médica, incluyendo su id.
     * @return mensaje de confirmación.
     * @throws BadRequestException si los datos son inválidos o la historia no existe.
     */
    MiRespuestaRS actualizarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException;
}