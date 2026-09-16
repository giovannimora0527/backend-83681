package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDateTime;
import java.util.List;


public interface CitaService {

    /**
     * Filtra las citas del sistema entre una fecha inicial y una fecha
     * final, ordenadas desde la más reciente hasta la más antigua.
     *
     * @param fechaInicial fecha inicial del filtro.
     * @param fechaFinal   fecha final del filtro.
     * @return lista de citas dentro del rango solicitado.
     * @throws BadRequestException si las fechas no son válidas.
     */
    List<Cita> filtrarCitasPorFecha(LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws BadRequestException;

    /**
     * Registra una nueva cita en el sistema.
     *
     * @param citaRq datos de la cita a crear.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos de entrada no son válidos.
     */
    MiRespuestaRS guardarCita(CitaRq citaRq) throws BadRequestException;

    /**
     * Actualiza una cita ya existente en el sistema.
     *
     * @param citaRq datos actualizados de la cita.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos no son válidos o la cita no existe.
     */
    MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException;
}