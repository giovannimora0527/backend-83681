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
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Manejador centralizado de errores de la API.
 * Convierte cada excepción en una respuesta {@link ErrorResponse} con el
 * código HTTP adecuado y un mensaje comprensible, sin exponer detalles
 * técnicos (trazas, nombres de clases o SQL) al usuario.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Logger para registrar los errores inesperados en la consola del servidor.
     */
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja los errores de negocio lanzados por los servicios
     * (400, 404 y 409).
     *
     * @param ex excepción de negocio.
     * @return respuesta con el código y mensaje de la excepción.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        return construirRespuesta(ex.getStatus(), ex.getMessage());
    }

    /**
     * Maneja cuerpos JSON mal formados o con tipos inválidos
     * (por ejemplo una fecha con formato incorrecto o texto en un campo numérico).
     *
     * @param ex excepción de lectura del cuerpo.
     * @return respuesta 400 con un mensaje genérico de formato.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST,
                "Los datos enviados no tienen un formato válido. Revise fechas, horas y valores numéricos.");
    }

    /**
     * Maneja parámetros de URL con tipo incorrecto (por ejemplo un id con letras).
     *
     * @param ex excepción de conversión de tipo.
     * @return respuesta 400 indicando el parámetro inválido.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST,
                "El valor del parámetro '" + ex.getName() + "' no es válido.");
    }

    /**
     * Maneja parámetros obligatorios ausentes en la URL.
     *
     * @param ex excepción de parámetro faltante.
     * @return respuesta 400 indicando el parámetro faltante.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getParameterName() + "' es obligatorio.");
    }

    /**
     * Maneja rutas que no existen en la API.
     *
     * @param ex excepción de recurso no encontrado.
     * @return respuesta 404.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, "El servicio solicitado no existe.");
    }

    /**
     * Maneja peticiones con un método HTTP no soportado por el endpoint.
     *
     * @param ex excepción de método no soportado.
     * @return respuesta 405.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return construirRespuesta(HttpStatus.METHOD_NOT_ALLOWED,
                "El método " + ex.getMethod() + " no está permitido para este servicio.");
    }

    /**
     * Maneja peticiones con un Content-Type distinto de JSON.
     *
     * @param ex excepción de tipo de contenido no soportado.
     * @return respuesta 415.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaType(HttpMediaTypeNotSupportedException ex) {
        return construirRespuesta(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "El contenido de la petición debe enviarse en formato JSON.");
    }

    /**
     * Maneja violaciones de integridad de la base de datos (llaves foráneas
     * o índices únicos) que no hayan sido controladas antes por el servicio.
     *
     * @param ex excepción de integridad de datos.
     * @return respuesta 409.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        LOG.warn("Violación de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return construirRespuesta(HttpStatus.CONFLICT,
                "La operación no se puede realizar porque afecta la integridad de la información registrada.");
    }

    /**
     * Maneja cualquier otro error no previsto. El detalle técnico se registra
     * en el log del servidor y al usuario solo se le muestra un mensaje genérico.
     *
     * @param ex excepción inesperada.
     * @return respuesta 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        LOG.error("Error inesperado en la API", ex);
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Intente nuevamente más tarde.");
    }

    /**
     * Construye la respuesta de error estándar.
     *
     * @param status  código HTTP de la respuesta.
     * @param message mensaje para el usuario.
     * @return respuesta HTTP con el cuerpo {@link ErrorResponse}.
     */
    private ResponseEntity<ErrorResponse> construirRespuesta(HttpStatus status, String message) {
        ErrorResponse error = new ErrorResponse(status.value(), status.getReasonPhrase().toUpperCase(), message);
        return ResponseEntity.status(status).body(error);
    }
}
