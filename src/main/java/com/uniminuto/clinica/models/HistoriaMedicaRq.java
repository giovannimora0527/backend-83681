package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Objeto de solicitud (request) para crear o actualizar una historia
 * médica.
 */
@Data
public class HistoriaMedicaRq {

    /**
     * Identificador de la historia médica. Requerido únicamente al
     * actualizar.
     */
    private Long historiaId;

    /**
     * Diagnóstico registrado en la historia médica.
     */
    private String diagnostico;

    /**
     * Tratamiento indicado en la historia médica.
     */
    private String tratamiento;

    /**
     * Identificador de la mascota a la que pertenece la historia médica.
     */
    private Long mascotaId;

    /**
     * Identificador del médico que registra la historia médica.
     */
    private Long medicoId;
}
