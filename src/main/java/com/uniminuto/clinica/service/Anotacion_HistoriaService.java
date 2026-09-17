package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Anotacion_Historia;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.UsuarioRS;
import com.uniminuto.clinica.models.Anotacion_HistoriaRq;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para operaciones CRUD limitadas sobre anotaciones de historia médica.
 */
public interface Anotacion_HistoriaService {

    /**
     * Crea una nueva anotación para una historia existente.
     *
     * @param rq objeto de petición con historiaId, medicoId y descripcion
     * @return UsuarioRS con status y mensaje del resultado
     */
    UsuarioRS guardarAnotacion(Anotacion_HistoriaRq rq);

    /**
     * Actualiza una anotación existente.
     *
     * @param rq objeto de petición con id y campos a actualizar
     * @return UsuarioRS con status y mensaje del resultado
     */
    UsuarioRS actualizarAnotacion(Anotacion_HistoriaRq rq);

    /**
     * Lista las anotaciones asociadas a una historia ordenadas por fecha descendente.
     *
     * @param historiaId identificador de la historia
     * @return lista de anotaciones
     */
    List<Anotacion_Historia> listarAnotacionesByHistoria(Long historiaId);

    /**
     * Lista las anotaciones de una historia dentro de un rango de fechas.
     *
     * @param historiaId identificador de la historia
     * @param fechaInicial fecha inicial del rango (inclusive)
     * @param fechaFinal fecha final del rango (inclusive)
     * @return lista de anotaciones filtradas por fecha
     */
    List<Anotacion_Historia> listarAnotacionesByHistoriaYFecha(Long historiaId, LocalDateTime fechaInicial, LocalDateTime fechaFinal);

    /**
     * Lista las anotaciones de una historia dentro de un rango de fechas recibido en texto.
     *
     * @param historiaId identificador de la historia
     * @param fechaInicial fecha inicial del rango en texto
     * @param fechaFinal fecha final del rango en texto
     * @return lista de anotaciones filtradas
     * @throws BadRequestException si el texto de fecha es inválido
     */
    List<Anotacion_Historia> filtrarAnotaciones(Long historiaId, String fechaInicial, String fechaFinal) throws BadRequestException;
}
