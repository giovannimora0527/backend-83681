package com.uniminuto.clinica.exception.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Estructura estándar de la respuesta de error que retorna el
 * {@link com.uniminuto.clinica.exception.GlobalExceptionHandler}.
 * Todos los errores de la API tienen esta misma forma, lo que permite al
 * frontend mostrarlos de manera uniforme.
 */
@Data
public class ErrorResponse {

    /**
     * Código HTTP del error (400, 404, 409, 500...).
     */
    private int status;

    /**
     * Nombre corto del tipo de error (por ejemplo "NOT FOUND").
     */
    private String error;

    /**
     * Mensaje comprensible para el usuario final.
     */
    private String message;

    /**
     * Fecha y hora en la que se produjo el error.
     */
    private LocalDateTime timestamp;

    /**
     * Crea una respuesta de error con la fecha y hora actual.
     *
     * @param status  código HTTP del error.
     * @param error   nombre corto del tipo de error.
     * @param message mensaje comprensible para el usuario.
     */
    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
