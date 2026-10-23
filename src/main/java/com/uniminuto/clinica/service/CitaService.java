package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.CitaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRequest;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Operaciones de negocio disponibles para las citas medicas.
 */
public interface CitaService {

    /**
     * Lista todas las citas ordenadas por fecha y hora descendente.
     *
     * @return lista de citas.
     */
    List<CitaMedica> listarCitas();

    /**
     * Consulta una cita por id.
     *
     * @param citaId id de la cita.
     * @return cita encontrada.
     * @throws BadRequestException si no existe (404).
     */
    CitaMedica consultarCita(Long citaId) throws BadRequestException;

    /**
     * Lista las citas de una mascota.
     *
     * @param mascotaId id de la mascota.
     * @return citas de la mascota.
     * @throws BadRequestException si la mascota no existe (404).
     */
    List<CitaMedica> listarCitasPorMascota(Long mascotaId) throws BadRequestException;

    /**
     * Lista las citas en un rango de fechas.
     *
     * @param fechaInicio fecha inicial.
     * @param fechaFinal fecha final.
     * @return citas en el rango.
     * @throws BadRequestException si el rango es invalido.
     */
    List<CitaMedica> filtrarCitasPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFinal)
            throws BadRequestException;

    /**
     * Crea una cita aplicando todas las reglas de negocio.
     *
     * @param citaRq datos de la cita.
     * @return respuesta de exito.
     * @throws BadRequestException si alguna regla no se cumple.
     */
    MiRespuestaRS crearCita(CitaRequest citaRq) throws BadRequestException;

    /**
     * Actualiza una cita teniendo en cuenta su estado actual.
     *
     * @param citaRq datos de la cita (con id).
     * @return respuesta de exito.
     * @throws BadRequestException si alguna regla no se cumple.
     */
    MiRespuestaRS actualizarCita(CitaRequest citaRq) throws BadRequestException;

    /**
     * Reactiva una cita cancelada (operacion explicita).
     *
     * @param citaId id de la cita.
     * @return respuesta de exito.
     * @throws BadRequestException si la cita no se puede reactivar.
     */
    MiRespuestaRS reactivarCita(Long citaId) throws BadRequestException;
}
