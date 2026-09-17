package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato de los servicios de negocio de la entidad HistoriaMedica.
 */
public interface HistoriaMedicaService {

    /**
     * Lista las historias medicas creadas entre dos fechas, de la mas reciente
     * a la mas antigua. Si no se envia el rango se listan todas.
     *
     * @param fechaInicial Fecha inicial del rango, puede ser nula.
     * @param fechaFinal Fecha final del rango, puede ser nula.
     * @return Lista de historias medicas ordenadas descendentemente.
     * @throws BadRequestException Si el rango de fechas es incompleto o invalido.
     */
    List<HistoriaMedica> listarHistoriasMedicas(LocalDate fechaInicial,
                                                LocalDate fechaFinal)
            throws BadRequestException;

    /**
     * Crea una nueva historia medica.
     *
     * @param historiaMedicaRq Datos de la historia medica a crear.
     * @return Respuesta con el estado de la operacion.
     * @throws BadRequestException Si los datos de entrada no son validos.
     */
    MiRespuestaRS guardarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq)
            throws BadRequestException;

    /**
     * Actualiza una historia medica existente.
     *
     * @param historiaMedicaRq Datos de la historia medica a actualizar.
     * @return Respuesta con el estado de la operacion.
     * @throws BadRequestException Si los datos no son validos o la historia no
     *                             existe.
     */
    MiRespuestaRS actualizarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq)
            throws BadRequestException;
}
