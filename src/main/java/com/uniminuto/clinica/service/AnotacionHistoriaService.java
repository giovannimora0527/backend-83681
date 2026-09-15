package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.models.UsuarioRS;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;

import java.util.List;

/**
 * Servicio para operaciones CRUD limitadas sobre anotaciones de historia médica.
 */
public interface AnotacionHistoriaService {

    /**
     * Crea una nueva anotación para una historia existente.
     * @param rq objeto de petición con historiaId, medicoId y descripcion
     * @return UsuarioRS con status y mensaje del resultado
     */
    UsuarioRS guardarAnotacion(AnotacionHistoriaRq rq);

    /**
     * Actualiza una anotación existente. Solo campos permitidos serán actualizados.
     * @param rq objeto de petición con id y campos a actualizar
     * @return UsuarioRS con status y mensaje del resultado
     */
    UsuarioRS actualizarAnotacion(AnotacionHistoriaRq rq);

    /**
     * Lista las anotaciones asociadas a una historia ordenadas por fecha descendente.
     * @param historiaId identificador de la historia
     * @return lista de anotaciones
     */
    List<AnotacionHistoria> listarAnotacionesByHistoria(Long historiaId);
}
