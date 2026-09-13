package com.uniminuto.clinica.service;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.CitaRs;

import java.time.LocalDate;
import java.util.List;

public interface CitaService {

    /**
     * Filtra las citas dentro de un rango de fechas especificado.
     *
     * @param fechaInicio Fecha de inicio para la búsqueda.
     * @param fechaFin Fecha de fin para la búsqueda.
     * @return Lista de citas mapeadas en Entity.
     * @throws BadRequestException si las fechas son inválidas.
     */
    List<CitaRs> filtrarCitasPorFecha(LocalDate fechaInicio, LocalDate fechaFin)
            throws BadRequestException;

    /**
     * Crea una nueva cita médica en el sistema.
     *
     * @param citaRq Datos de la cita a crear.
     * @return DTO con la información de la cita creada.
     * @throws BadRequestException Si faltan datos obligatorios.
     */
    CitaRs crearCita(CitaRq citaRq) throws BadRequestException;

    /**
     * Actualiza la información de una cita existente.
     *
     * @param citaRq Datos a actualizar (debe incluir el id).
     * @return DTO con la cita actualizada.
     * @throws BadRequestException Si el ID no existe o faltan datos.
     */
    CitaRs actualizarCita(CitaRq citaRq) throws BadRequestException;
}
