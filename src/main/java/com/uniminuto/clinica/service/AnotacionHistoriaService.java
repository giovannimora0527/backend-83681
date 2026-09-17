package com.uniminuto.clinica.service;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.AnotacionHistoriaRs;

import java.time.LocalDate;
import java.util.List;

/**
 * Interfaz de contrato para los servicios de Anotación de Historia Médica.
 */

public interface AnotacionHistoriaService {

    /**
     * Lista anotaciones filtradas por rango de fechas, ordenadas de la más reciente a la más antigua.
     *
     * @param fechaInicio Fecha inicial del rango de filtrado (YYYY-MM-DD).
     * @param fechaFin Fecha final del rango de filtrado (YYYY-MM-DD).
     * @return Lista de DTOs de respuesta con los datos de las anotaciones.
     * @throws BadRequestException Si las fechas son nulas o la fecha inicial es mayor a la final.
     */

    List<AnotacionHistoriaRs> listarAnotaciones(LocalDate fechaInicio, LocalDate fechaFin) throws BadRequestException;

    AnotacionHistoriaRs crearAnotacion(AnotacionHistoriaRq anotacionRq) throws BadRequestException;

    AnotacionHistoriaRs actualizarAnotacion(AnotacionHistoriaRq anotacionRq) throws BadRequestException;
}
