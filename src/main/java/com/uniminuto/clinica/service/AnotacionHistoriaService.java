package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/**
 * Operaciones de negocio disponibles para las anotaciones de historia clinica.
 */
public interface AnotacionHistoriaService {

    /**
     * Agrega una anotacion a una historia clinica abierta.
     *
     * @param anotacionRq datos de la anotacion.
     * @return respuesta de exito.
     * @throws BadRequestException si alguna regla no se cumple.
     */
    MiRespuestaRS crearAnotacionHistoria(AnotacionRq anotacionRq) throws BadRequestException;

    /**
     * Lista las anotaciones de una historia, de la mas reciente a la mas antigua.
     *
     * @param historiaId id de la historia.
     * @return anotaciones.
     * @throws BadRequestException si la historia no existe (404).
     */
    List<AnotacionHistoria> listarPorHistoria(Long historiaId) throws BadRequestException;

    /**
     * Lista todas las anotaciones de una mascota, de la mas reciente a la mas antigua.
     *
     * @param mascotaId id de la mascota.
     * @return anotaciones.
     * @throws BadRequestException si la mascota no existe (404).
     */
    List<AnotacionHistoria> listarPorMascota(Long mascotaId) throws BadRequestException;
}
