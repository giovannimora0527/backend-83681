package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para la gestión de citas de la Clínica Veterinaria.
 */
public interface CitaService {

    /**
     * Filtra las citas del sistema dada una fecha inicial y una fecha
     * final. El resultado se organiza desde la cita más reciente hasta
     * la más antigua.
     *
     * @param fechaInicial fecha inicial del rango a filtrar.
     * @param fechaFinal   fecha final del rango a filtrar.
     * @return lista de citas dentro del rango solicitado.
     * @throws BadRequestException si las fechas son inválidas.
     */
    List<Cita> filtrarCitasPorFecha(LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws BadRequestException;

    /**
     * Crea una nueva cita en el sistema.
     *
     * @param citaRq datos de la cita a crear.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos de entrada son inválidos.
     */
    MiRespuestaRS crearCita(CitaRq citaRq) throws BadRequestException;

    /**
     * Actualiza una cita existente en el sistema.
     *
     * @param citaRq datos de la cita a actualizar.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos de entrada son inválidos o
     *                              la cita no existe.
     */
    MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException;
}
