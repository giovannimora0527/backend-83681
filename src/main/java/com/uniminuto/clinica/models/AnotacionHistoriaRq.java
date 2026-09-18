package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Objeto de solicitud (request) para crear o actualizar una anotación
 * de historia médica.
 */
@Data
public class AnotacionHistoriaRq {

    /**
     * Identificador de la anotación. Requerido únicamente al actualizar.
     */
    private Long anotacionId;

    /**
     * Observación u anotación realizada sobre la historia médica.
     */
    private String observacion;

    /**
     * Identificador de la historia médica a la que pertenece la
     * anotación.
     */
    private Long historiaId;

    /**
     * Identificador del médico que realiza la anotación.
     */
    private Long medicoId;
}
