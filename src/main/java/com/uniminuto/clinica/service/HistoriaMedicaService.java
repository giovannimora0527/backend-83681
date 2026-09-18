package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para la gestión de historias médicas de la Clínica
 * Veterinaria.
 */
public interface HistoriaMedicaService {

    /**
     * Crea una nueva historia médica en el sistema.
     *
     * @param historiaMedicaRq datos de la historia médica a crear.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos de entrada son inválidos.
     */
    MiRespuestaRS crearHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException;

    /**
     * Lista las historias médicas creadas entre una fecha inicial y una
     * fecha final, organizadas de la más reciente a la más antigua.
     *
     * @param fechaInicial fecha inicial del rango a filtrar.
     * @param fechaFinal   fecha final del rango a filtrar.
     * @return lista de historias médicas dentro del rango solicitado.
     * @throws BadRequestException si las fechas son inválidas.
     */
    List<HistoriaMedica> listarHistoriasPorFecha(LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws BadRequestException;

    /**
     * Actualiza una historia médica existente en el sistema.
     *
     * @param historiaMedicaRq datos de la historia médica a actualizar.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos de entrada son inválidos o
     *                              la historia médica no existe.
     */
    MiRespuestaRS actualizarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException;
}
