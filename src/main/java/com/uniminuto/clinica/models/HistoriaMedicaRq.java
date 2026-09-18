package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Objeto de entrada (request) para crear o actualizar una historia médica.
 * Cuando se usa para actualizar, el campo "id" es obligatorio.
 */
@Data
public class HistoriaMedicaRq {

    /**
     * Identificador de la historia médica. Solo se usa (y es obligatorio) al actualizar.
     */
    private Long id;

    /**
     * Identificador del paciente (mascota) dueño de la historia médica.
     */
    private Long pacienteId;
}