package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepcion para cuando la operacion choca con el estado actual de los datos
 * (HTTP 409 Conflict). Ejemplo: veterinario no disponible, cita duplicada,
 * intentar modificar una cita cancelada o atendida.
 */
public class ConflictException extends BadRequestException {

    /**
     * Crea la excepcion con codigo HTTP 409.
     *
     * @param message mensaje claro para el usuario.
     */
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
