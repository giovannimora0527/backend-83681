package com.uniminuto.clinica.exception;

/**
 * Se lanza cuando el registro solicitado no existe (respuesta HTTP 404).
 */
public class NotFoundException extends RuntimeException {

    /**
     * @param message mensaje claro para el usuario.
     */
    public NotFoundException(String message) {
        super(message);
    }
}
