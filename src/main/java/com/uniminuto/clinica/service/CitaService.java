package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaService {

    /**
     * Filtra las citas cuya fecha_hora está entre fechaInicio y fechaFin.
     *
     * @param fechaInicio fecha y hora inicial del rango a filtrar.
     * @param fechaFin    fecha y hora final del rango a filtrar.
     * @return lista de citas dentro del rango, de la más reciente a la más antigua.
     * @throws BadRequestException si las fechas no son válidas.
     */
    List<Cita> filtrarCitasPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws BadRequestException;

    /**
     * Registra una nueva cita en el sistema.
     *
     * @param citaRq datos de la cita a crear.
     * @return mensaje de confirmación.
     * @throws BadRequestException si los datos son inválidos o el cliente/mascota/médico no existen.
     */
    MiRespuestaRS guardarCita(CitaRq citaRq) throws BadRequestException;

    /**
     * Actualiza una cita ya existente.
     *
     * @param citaRq datos nuevos de la cita, incluyendo su id.
     * @return mensaje de confirmación.
     * @throws BadRequestException si los datos son inválidos o la cita no existe.
     */
    MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException;
}