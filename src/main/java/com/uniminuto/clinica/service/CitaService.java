package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato de los servicios de negocio de la entidad Cita.
 */
public interface CitaService {

    /**
     * Filtra las citas registradas entre dos fechas y las devuelve de la mas
     * reciente a la mas antigua.
     *
     * @param fechaInicial Fecha inicial del rango.
     * @param fechaFinal Fecha final del rango.
     * @return Lista de citas del rango ordenadas descendentemente.
     * @throws BadRequestException Si el rango de fechas no es valido.
     */
    List<Cita> listarCitasPorRangoFechas(LocalDate fechaInicial, LocalDate fechaFinal)
            throws BadRequestException;

    /**
     * Guarda una nueva cita en el sistema.
     *
     * @param citaRq Datos de la cita a crear.
     * @return Respuesta con el estado de la operacion.
     * @throws BadRequestException Si los datos de entrada no son validos.
     */
    MiRespuestaRS guardarCita(CitaRq citaRq) throws BadRequestException;

    /**
     * Actualiza una cita ya almacenada en el sistema.
     *
     * @param citaRq Datos de la cita a actualizar.
     * @return Respuesta con el estado de la operacion.
     * @throws BadRequestException Si los datos de entrada no son validos o la
     *                             cita no existe.
     */
    MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException;
}
