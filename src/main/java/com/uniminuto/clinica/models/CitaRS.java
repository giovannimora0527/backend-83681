package com.uniminuto.clinica.models;

import lombok.Data;


/**
 * Modelo de respuesta para operaciones relacionadas con citas.
 */
@Data
public class CitaRS {

    /** Código numérico de resultado (e.j. 200 para éxito). */
    private int status;

    /** Mensaje legible con información del resultado. */
    private String message;
}
