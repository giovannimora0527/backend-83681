package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Modelo de respuesta genérico usado por varios endpoints para indicar status y mensaje.
 */
@Data
public class UsuarioRS {

    /** Código numérico de resultado (e.j. 200 para éxito). */
    private int status;

    /** Mensaje legible con información del resultado. */
    private String message;
}
