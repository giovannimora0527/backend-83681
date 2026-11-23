package com.uniminuto.clinica.exception;

/**
 * Se lanza cuando la operación choca con el estado actual de los datos:
 * horarios ocupados, citas duplicadas o registros que ya no pueden modificarse (respuesta HTTP 409).
 */
public class ConflictException extends RuntimeException {

    /**
     * @param message mensaje claro para el usuario.
     */
    public ConflictException(String message) {
        super(message);
    }
}
