package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoriaMedicaService {

    /**
     * Registra una nueva historia médica en el sistema.
     *
     * @param historiaMedicaRq datos de la historia médica a crear.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos de entrada no son válidos.
     */
    MiRespuestaRS crearHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException;

    /**
     * Lista las historias médicas cuya fecha de creación esté entre una
     * fecha inicial y una fecha final, ordenadas de la más reciente a la
     * más antigua.
     *
     * @param fechaInicial fecha inicial del filtro.
     * @param fechaFinal   fecha final del filtro.
     * @return lista de historias médicas dentro del rango solicitado.
     * @throws BadRequestException si las fechas no son válidas.
     */
    List<HistoriaMedica> listarHistoriasMedicas(LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws BadRequestException;

    /**
     * Actualiza una historia médica ya existente en el sistema.
     *
     * @param historiaMedicaRq datos actualizados de la historia médica.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos no son válidos o la historia no existe.
     */
    MiRespuestaRS actualizarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException;
}