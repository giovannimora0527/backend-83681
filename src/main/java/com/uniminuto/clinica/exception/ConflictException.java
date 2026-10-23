package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción para operaciones que entran en conflicto con el estado actual de
 * la información (HTTP 409 - Conflict). Ejemplos: veterinario no disponible,
 * cita duplicada o intento de modificar una cita cancelada o atendida.
 */
public class ConflictException extends ApiException {

    /**
     * Crea la excepción con el mensaje indicado.
     *
     * @param message mensaje descriptivo del error.
     */
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
