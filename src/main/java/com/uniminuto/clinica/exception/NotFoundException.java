package com.uniminuto.clinica.exception;

/**
 * Excepción que indica que un recurso solicitado no existe (HTTP 404).
 */
public class NotFoundException extends RuntimeException {

    /**
     * Crea la excepción con el mensaje que verá el usuario.
     *
     * @param message descripción del recurso no encontrado.
     */
    public NotFoundException(String message) {
        super(message);
    }
}
