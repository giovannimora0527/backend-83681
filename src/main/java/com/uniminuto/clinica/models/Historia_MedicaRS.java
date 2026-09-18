package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Modelo de respuesta para operaciones relacionadas con historias médicas.
 */
@Data
public class Historia_MedicaRS {

    /** Código numérico de resultado (por ejemplo 200 para éxito). */
    private int status;

    /** Mensaje legible con información del resultado de la operación. */
    private String message;
}
