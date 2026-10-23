package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaClinicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/**
 * Operaciones de negocio disponibles para las historias clinicas.
 */
public interface HistoriaClinicaService {

    /**
     * Lista las historias ordenadas por fecha de consulta descendente.
     *
     * @return lista de historias.
     */
    List<HistoriaMedica> listarHistorias();

    /**
     * Consulta una historia por id.
     *
     * @param historiaId id de la historia.
     * @return historia encontrada.
     * @throws BadRequestException si no existe (404).
     */
    HistoriaMedica consultarHistoria(Long historiaId) throws BadRequestException;

    /**
     * Crea una historia clinica aplicando las reglas de negocio.
     *
     * @param historiaRq datos de la historia.
     * @return respuesta de exito.
     * @throws BadRequestException si alguna regla no se cumple.
     */
    MiRespuestaRS crearHistoria(HistoriaClinicaRq historiaRq) throws BadRequestException;

    /**
     * Cierra una historia para que no reciba mas anotaciones.
     *
     * @param historiaId id de la historia.
     * @return respuesta de exito.
     * @throws BadRequestException si ya estaba cerrada.
     */
    MiRespuestaRS cerrarHistoria(Long historiaId) throws BadRequestException;
}
