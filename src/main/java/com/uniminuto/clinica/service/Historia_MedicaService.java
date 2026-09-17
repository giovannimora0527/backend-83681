package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Historia_Medica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.Historia_MedicaRq;
import com.uniminuto.clinica.models.UsuarioRS;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio que expone operaciones sobre historias médicas.
 */
public interface Historia_MedicaService {

    /**
     * Filtra historias por un rango de fechas. Si inicio o fin son nulos, devuelve todas las historias
     * ordenadas de la más reciente a la más antigua.
     * @param inicio fecha/hora inicial del rango (inclusive) o null
     * @param fin fecha/hora final del rango (inclusive) o null
     * @return lista de historias que cumplen el filtro
     */
    List<Historia_Medica> filtrarHistorias(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Guarda una nueva historia médica.
     *
     * @param historiaMedicaRq entidad a guardar
     * @return respuesta con el estado del proceso
     * @throws BadRequestException si la información es inválida
     */
    UsuarioRS guardarHistoria(Historia_MedicaRq historiaMedicaRq) throws BadRequestException;

    /**
     * Actualiza una historia médica existente.
     *
     * @param historiaMedicaRq datos con los cambios
     * @return respuesta con el estado del proceso
     * @throws BadRequestException si la información es inválida
     */
    UsuarioRS actualizarHistoria(Historia_MedicaRq historiaMedicaRq) throws BadRequestException;
}
