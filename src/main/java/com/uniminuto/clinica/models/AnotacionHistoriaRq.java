package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * DTO para la recepción de datos de creación y actualización de anotaciones.
 */
@Data
public class AnotacionHistoriaRq {

    /** Identificador de la anotación (obligatorio solo para actualizar). */
    private Long id;

    /** Identificador de la historia clínica a la que pertenece la anotación. */
    private Long historiaId;

    /** Identificador del médico que realizó la anotación (obligatorio). */
    private Integer medicoId;
    private String descripcion;
}
