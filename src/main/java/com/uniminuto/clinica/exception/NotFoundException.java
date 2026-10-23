package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepcion para cuando un registro buscado no existe (HTTP 404 Not Found).
 * Ejemplo: cliente inexistente, mascota inexistente, cita no encontrada.
 */
public class NotFoundException extends BadRequestException {

    /**
     * Crea la excepcion con codigo HTTP 404.
     *
     * @param message mensaje claro para el usuario.
     */
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
