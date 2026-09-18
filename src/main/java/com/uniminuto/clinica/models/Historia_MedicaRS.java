package com.uniminuto.clinica.models;

import lombok.Data;
/**
 * Modelo de respuesta para la historia médica.
 */
@Data
public class Historia_MedicaRS {

    /** Código numérico de resultado (e.j. 200 para éxito). */
    private int status;

    /** Mensaje legible con información del resultado. */
    private String message;

}
