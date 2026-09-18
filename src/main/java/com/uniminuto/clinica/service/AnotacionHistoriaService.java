package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDateTime;
import java.util.List;

public interface AnotacionHistoriaService {

    /**
     * Lista las anotaciones cuya fecha está entre fechaInicio y fechaFin.
     *
     * @param fechaInicio fecha y hora inicial del rango a filtrar.
     * @param fechaFin    fecha y hora final del rango a filtrar.
     * @return lista de anotaciones dentro del rango, de la más reciente a la más antigua.
     * @throws BadRequestException si las fechas no son válidas.
     */
    List<AnotacionHistoria> listarAnotaciones(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws BadRequestException;

    /**
     * Crea una nueva anotación dentro de una historia médica.
     *
     * @param anotacionHistoriaRq datos de la anotación a crear.
     * @return mensaje de confirmación.
     * @throws BadRequestException si los datos son inválidos o la historia/médico no existen.
     */
    MiRespuestaRS guardarAnotacion(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException;

    /**
     * Actualiza una anotación ya existente.
     *
     * @param anotacionHistoriaRq datos nuevos de la anotación, incluyendo su id.
     * @return mensaje de confirmación.
     * @throws BadRequestException si los datos son inválidos o la anotación no existe.
     */
    MiRespuestaRS actualizarAnotacion(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException;
}