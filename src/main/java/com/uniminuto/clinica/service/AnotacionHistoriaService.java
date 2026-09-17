package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/**
 * Contrato de los servicios de negocio de la entidad AnotacionHistoria.
 */
public interface AnotacionHistoriaService {

    /**
     * Lista las anotaciones de historia ordenadas de la mas reciente a la mas
     * antigua. Si se envia el id de una historia medica solo se listan las
     * anotaciones que le pertenecen.
     *
     * @param historiaId Identificador de la historia medica, puede ser nulo.
     * @return Lista de anotaciones ordenadas descendentemente.
     * @throws BadRequestException Si la historia medica indicada no existe.
     */
    List<AnotacionHistoria> listarAnotaciones(Long historiaId)
            throws BadRequestException;

    /**
     * Crea una nueva anotacion asociada a una historia medica.
     *
     * @param anotacionHistoriaRq Datos de la anotacion a crear.
     * @return Respuesta con el estado de la operacion.
     * @throws BadRequestException Si los datos de entrada no son validos.
     */
    MiRespuestaRS guardarAnotacion(AnotacionHistoriaRq anotacionHistoriaRq)
            throws BadRequestException;

    /**
     * Actualiza una anotacion existente.
     *
     * @param anotacionHistoriaRq Datos de la anotacion a actualizar.
     * @return Respuesta con el estado de la operacion.
     * @throws BadRequestException Si los datos no son validos o la anotacion no
     *                             existe.
     */
    MiRespuestaRS actualizarAnotacion(AnotacionHistoriaRq anotacionHistoriaRq)
            throws BadRequestException;
}
