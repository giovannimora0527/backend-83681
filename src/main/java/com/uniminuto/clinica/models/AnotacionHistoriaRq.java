package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Objeto de entrada (DTO) usado para crear o actualizar una anotacion de
 * historia medica.
 */
@Data
public class AnotacionHistoriaRq {

    /** Identificador de la anotacion. Solo se usa al actualizar. */
    private Long anotacionId;

    /** Texto de la anotacion. */
    private String descripcion;

    /** Identificador de la historia medica a la que pertenece la anotacion. */
    private Long historiaId;
}
