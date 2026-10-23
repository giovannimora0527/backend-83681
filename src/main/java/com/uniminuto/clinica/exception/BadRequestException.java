package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/**
 * Clase de excepción personalizada para manejar errores de solicitud incorrecta (Bad Request).
 * Es la excepcion base del proyecto: las demas excepciones de negocio
 * (NotFoundException, ConflictException) la extienden cambiando el codigo HTTP.
 */
public class BadRequestException extends RuntimeException {

    /**
     * Codigo HTTP que se devolvera al cliente.
     */
    private final HttpStatus status;

    /**
     * Crea una excepcion con codigo HTTP 400 (Bad Request).
     *
     * @param message mensaje claro para el usuario.
     */
    public BadRequestException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Crea una excepcion con un codigo HTTP especifico.
     * Solo la usan las clases hijas.
     *
     * @param message mensaje claro para el usuario.
     * @param status codigo HTTP que se devolvera.
     */
    protected BadRequestException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Obtiene el codigo HTTP asociado a la excepcion.
     *
     * @return codigo HTTP.
     */
    public HttpStatus getStatus() {
        return status;
    }
}
