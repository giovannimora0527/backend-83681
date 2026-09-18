package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/**
 * Servicio para la gestión de anotaciones de historia médica de la
 * Clínica Veterinaria.
 */
public interface AnotacionHistoriaService {

    /**
     * Crea una nueva anotación de historia médica.
     *
     * @param anotacionHistoriaRq datos de la anotación a crear.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos de entrada son inválidos.
     */
    MiRespuestaRS crearAnotacion(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException;

    /**
     * Lista las anotaciones asociadas a una historia médica, ordenadas
     * de la más reciente a la más antigua.
     *
     * @param historiaId identificador de la historia médica.
     * @return lista de anotaciones de la historia médica.
     * @throws BadRequestException si el identificador es inválido o la
     *                              historia médica no existe.
     */
    List<AnotacionHistoria> listarAnotacionesPorHistoria(Long historiaId) throws BadRequestException;

    /**
     * Actualiza una anotación de historia médica existente.
     *
     * @param anotacionHistoriaRq datos de la anotación a actualizar.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos de entrada son inválidos o
     *                              la anotación no existe.
     */
    MiRespuestaRS actualizarAnotacion(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException;
}
