package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.UsuarioRS;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Servicio que expone operaciones sobre citas.
 */
public interface CitaService {

    /**
     * Retorna las citas dentro del rango de fechas indicado, ordenadas
     * desde la más reciente hasta la más antigua.
     *
     * @param fechaInicial Fecha inicial del rango de búsqueda. Si es null, se interpretará como sin filtro.
     * @param fechaFinal Fecha final del rango de búsqueda. Si es null, se interpretará como sin filtro.
     * @return Lista de citas que cumplen el rango establecido.
     */
    List<Cita> filtrarCitas(LocalDateTime fechaInicial, LocalDateTime fechaFinal);

    /**
     * Retorna las citas dentro de un rango recibido en texto y lo convierte a LocalDateTime.
     *
     * @param fechaInicial Fecha inicial en formato yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss.
     * @param fechaFinal Fecha final en formato yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss.
     * @return Lista de citas dentro del rango indicado.
     * @throws BadRequestException Si alguna de las fechas es nula, vacía o tiene formato inválido.
     */
    List<Cita> filtrarCitas(String fechaInicial, String fechaFinal) throws BadRequestException;

    /**
     * Guarda una nueva cita validando los datos recibidos y la existencia de
     * cliente, médico y mascota relacionados.
     *
     * @param citaRq Objeto con los datos de la cita a registrar.
     * @return Respuesta con el estado y el mensaje de la operación.
     * @throws BadRequestException Si los datos enviados son inválidos o no existen entidades relacionadas.
     */
    UsuarioRS guardarCita(CitaRq citaRq) throws BadRequestException;

    /**
     * Actualiza una cita existente validando la información enviada.
     *
     * @param citaRq Objeto con los datos actualizados de la cita.
     * @return Respuesta con el estado y el mensaje de la operación.
     * @throws BadRequestException Si la cita no existe o los datos no son válidos.
     */
    UsuarioRS actualizarCita(CitaRq citaRq) throws BadRequestException;
}
