package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción de negocio que se traduce en una respuesta HTTP controlada.
 * Por defecto responde 400 (Bad Request); también permite 404 (no encontrado)
 * y 409 (conflicto con una regla de negocio).
 */
public class BadRequestException extends RuntimeException {

    private final HttpStatus status;

    /**
     * Crea una excepción con estado 400 (Bad Request).
     *
     * @param message mensaje claro para el usuario.
     */
    public BadRequestException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Crea una excepción con un estado HTTP específico.
     *
     * @param message mensaje claro para el usuario.
     * @param status  código HTTP a devolver.
     */
    public BadRequestException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * @return código HTTP asociado a la excepción.
     */
    public HttpStatus getStatus() {
        return status;
    }
}
