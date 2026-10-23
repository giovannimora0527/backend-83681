package com.uniminuto.clinica.exception;

import com.uniminuto.clinica.exception.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Manejador global de excepciones: traduce cada error a un código HTTP adecuado y a una respuesta
 * uniforme (status, error, message). Los mensajes dirigidos al usuario no exponen detalles técnicos;
 * el detalle de los errores inesperados solo se escribe en el log del servidor.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Registro de eventos para los errores inesperados. */
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Construye la respuesta de error estándar.
     *
     * @param estado código HTTP.
     * @param titulo nombre corto del error.
     * @param mensaje mensaje para el usuario.
     * @return respuesta HTTP con el cuerpo de error.
     */
    private ResponseEntity<ErrorResponse> responder(HttpStatus estado, String titulo, String mensaje) {
        return ResponseEntity.status(estado).body(new ErrorResponse(estado.value(), titulo, mensaje));
    }

    /**
     * Datos inválidos o referencias inexistentes en la solicitud (HTTP 400).
     *
     * @param ex excepción lanzada.
     * @return respuesta 400.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return responder(ex.getStatus(), "BAD REQUEST", ex.getMessage());
    }

    /**
     * Recurso inexistente (HTTP 404).
     *
     * @param ex excepción lanzada.
     * @return respuesta 404.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return responder(HttpStatus.NOT_FOUND, "NOT FOUND", ex.getMessage());
    }

    /**
     * Regla de negocio que impide la operación (HTTP 409).
     *
     * @param ex excepción lanzada.
     * @return respuesta 409.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return responder(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage());
    }

    /**
     * Restricción de integridad de la base de datos (duplicados, llaves foráneas) (HTTP 409).
     *
     * @param ex excepción lanzada.
     * @return respuesta 409.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegridad(DataIntegrityViolationException ex) {
        LOG.warn("Violación de integridad de datos: {}", ex.getMessage());
        return responder(HttpStatus.CONFLICT, "CONFLICT",
                "No fue posible guardar: la información ya existe o está relacionada con otros registros.");
    }

    /**
     * Cuerpo de la solicitud con formato inválido (JSON mal formado, fecha o número no válidos) (HTTP 400).
     *
     * @param ex excepción lanzada.
     * @return respuesta 400.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleCuerpoIlegible(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, "BAD REQUEST",
                "Los datos enviados no tienen el formato esperado. Verifique las fechas y los valores numéricos.");
    }

    /**
     * Parámetro de la ruta o de la consulta con tipo incorrecto (HTTP 400).
     *
     * @param ex excepción lanzada.
     * @return respuesta 400.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTipoParametro(MethodArgumentTypeMismatchException ex) {
        return responder(HttpStatus.BAD_REQUEST, "BAD REQUEST",
                "Un parámetro de la solicitud no tiene el formato esperado.");
    }

    /**
     * Ruta inexistente (HTTP 404).
     *
     * @param ex excepción lanzada.
     * @return respuesta 404.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleRutaInexistente(NoResourceFoundException ex) {
        return responder(HttpStatus.NOT_FOUND, "NOT FOUND", "El recurso solicitado no existe.");
    }

    /**
     * Método HTTP no permitido para la ruta (HTTP 405).
     *
     * @param ex excepción lanzada.
     * @return respuesta 405.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMetodoNoPermitido(HttpRequestMethodNotSupportedException ex) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "METHOD NOT ALLOWED",
                "La operación solicitada no está permitida para este recurso.");
    }

    /**
     * Tipo de contenido no soportado (HTTP 415).
     *
     * @param ex excepción lanzada.
     * @return respuesta 415.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleTipoContenido(HttpMediaTypeNotSupportedException ex) {
        return responder(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED MEDIA TYPE",
                "El formato de los datos enviados no es compatible.");
    }

    /**
     * Cualquier error no previsto (HTTP 500). El detalle técnico se registra en el log y
     * al usuario solo se le muestra un mensaje genérico.
     *
     * @param ex excepción lanzada.
     * @return respuesta 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        LOG.error("Error inesperado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR",
                "Ocurrió un error inesperado. Intente nuevamente en unos momentos.");
    }
}
