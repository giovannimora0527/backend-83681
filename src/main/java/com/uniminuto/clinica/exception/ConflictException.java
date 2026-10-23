package com.uniminuto.clinica.exception;

/**
 * Excepción que indica que la operación choca con el estado actual de la información
 * o con una regla de negocio (HTTP 409): horarios ocupados, citas duplicadas, información
 * que ya no puede modificarse, etc.
 */
public class ConflictException extends RuntimeException {

    /**
     * Crea la excepción con el mensaje que verá el usuario.
     *
     * @param message descripción de la regla de negocio incumplida.
     */
    public ConflictException(String message) {
        super(message);
    }
}
