package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Objeto de entrada (request) para crear o actualizar una anotación de historia médica.
 * Cuando se usa para actualizar, el campo "id" es obligatorio.
 */
@Data
public class AnotacionHistoriaRq {

    /**
     * Identificador de la anotación. Solo se usa (y es obligatorio) al actualizar.
     */
    private Long id;

    /**
     * Identificador de la historia médica a la que pertenece la anotación.
     */
    private Long historiaId;

    /**
     * Identificador del médico que registra la anotación.
     */
    private Long medicoId;

    /**
     * Descripción del hallazgo o novedad registrada por el médico.
     */
    private String descripcion;
}