package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaz del servicio para gestionar las anotaciones de historia médica.
 */
public interface AnotacionHistoriaService {

    /**
     * Crea una nueva anotación en el sistema.
     *
     * @param anotacion Objeto anotación a registrar.
     * @return Anotación creada.
     */
    AnotacionHistoria crearAnotacion(AnotacionHistoria anotacion);

    /**
     * Actualiza una anotación existente.
     *
     * @param id Identificador de la anotación a actualizar.
     * @param anotacion Datos actualizados.
     * @return Anotación modificada.
     */
    AnotacionHistoria actualizarAnotacion(Long id, AnotacionHistoria anotacion);

    /**
     * Obtiene la lista de anotaciones dentro de un rango de fechas ordenadas desc.
     *
     * @param fechaInicio Fecha inicial.
     * @param fechaFin Fecha final.
     * @return Lista de anotaciones filtradas y ordenadas.
     */
    List<AnotacionHistoria> listarPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}