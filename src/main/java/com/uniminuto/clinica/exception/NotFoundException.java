package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción para registros que no existen en la base de datos
 * (HTTP 404 - Not Found). Ejemplos: cliente, mascota, veterinario o cita
 * inexistentes.
 */
public class NotFoundException extends ApiException {

    /**
     * Crea la excepción con el mensaje indicado.
     *
     * @param message mensaje descriptivo del error.
     */
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
