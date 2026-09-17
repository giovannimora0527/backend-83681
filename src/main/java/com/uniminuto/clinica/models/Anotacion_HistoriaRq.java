package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Modelo de petición para crear o actualizar una anotación de historia.
 */
@Data
public class Anotacion_HistoriaRq {

    /** Identificador de la anotación (para actualizaciones). */
    private Long id;

    /** Identificador de la historia a la que pertenece la anotación. */
    private Long historiaId;

    /** Identificador del médico que realiza la anotación. */
    private Integer medicoId;

    /** Texto descriptivo de la anotación. */
    private String descripcion;
}
