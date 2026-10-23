package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción para datos de entrada inválidos o que incumplen una regla de
 * validación (HTTP 400 - Bad Request). Ejemplos: campos obligatorios vacíos,
 * fechas en el pasado o una mascota que no pertenece al cliente indicado.
 */
public class BadRequestException extends ApiException {

    /**
     * Crea la excepción con el mensaje indicado.
     *
     * @param message mensaje descriptivo del error.
     */
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
