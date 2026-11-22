package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción de negocio. Por defecto responde 400 (Bad Request), pero admite
 * otros códigos (401, 403, 404, 409) para que el backend informe correctamente
 * los errores de autenticación, permisos y conflictos de negocio.
 */
public class BadRequestException extends RuntimeException {

    private final HttpStatus status;

    public BadRequestException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public BadRequestException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
