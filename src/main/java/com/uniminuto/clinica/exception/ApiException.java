package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción base de la aplicación. Cada subclase representa un tipo de error
 * de negocio y define el código HTTP con el que debe responderse al cliente.
 * Es manejada de forma centralizada por {@link GlobalExceptionHandler}.
 */
public abstract class ApiException extends RuntimeException {

    /**
     * Código HTTP asociado al error.
     */
    private final HttpStatus status;

    /**
     * Crea una excepción con un mensaje entendible para el usuario.
     *
     * @param message mensaje descriptivo del error.
     * @param status  código HTTP con el que se responderá.
     */
    protected ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Obtiene el código HTTP asociado al error.
     *
     * @return código HTTP de la respuesta.
     */
    public HttpStatus getStatus() {
        return status;
    }
}
