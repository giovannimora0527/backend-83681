package com.uniminuto.clinica.exception;

import com.uniminuto.clinica.exception.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Captura todas las excepciones lanzadas por los controladores y las
 * convierte en una respuesta JSON uniforme: { status, error, message }.
 * Asi el frontend siempre recibe el mismo formato y mensajes entendibles,
 * sin exponer detalles tecnicos (stacktrace, SQL, nombres de clases).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Logger para registrar en consola los errores inesperados.
     */
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja las excepciones de negocio (400, 404 y 409).
     * NotFoundException y ConflictException tambien llegan aqui porque
     * extienden de BadRequestException.
     *
     * @param ex excepcion de negocio lanzada por un servicio.
     * @return respuesta con el codigo HTTP y el mensaje de la excepcion.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return construirRespuesta(ex.getStatus(), ex.getMessage());
    }

    /**
     * Maneja un JSON mal formado o con tipos incorrectos
     * (por ejemplo una fecha con formato invalido o texto en un campo numerico).
     *
     * @param ex excepcion de lectura del cuerpo de la peticion.
     * @return respuesta 400 con un mensaje entendible.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonInvalido(HttpMessageNotReadableException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST,
                "Los datos enviados no tienen un formato válido. Verifique fechas y valores numéricos.");
    }

    /**
     * Maneja parametros de la URL con un tipo incorrecto (ej: citaId=abc).
     *
     * @param ex excepcion de conversion del parametro.
     * @return respuesta 400 indicando el parametro invalido.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleParametroInvalido(MethodArgumentTypeMismatchException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getName() + "' tiene un valor inválido.");
    }

    /**
     * Maneja parametros obligatorios que no se enviaron en la URL.
     *
     * @param ex excepcion de parametro faltante.
     * @return respuesta 400 indicando el parametro faltante.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleParametroFaltante(MissingServletRequestParameterException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getParameterName() + "' es obligatorio.");
    }

    /**
     * Maneja peticiones con un metodo HTTP no permitido (ej: GET a un endpoint POST).
     *
     * @param ex excepcion de metodo no soportado.
     * @return respuesta 405.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMetodoNoPermitido(HttpRequestMethodNotSupportedException ex) {
        return construirRespuesta(HttpStatus.METHOD_NOT_ALLOWED,
                "El método HTTP " + ex.getMethod() + " no está permitido para este servicio.");
    }

    /**
     * Maneja peticiones enviadas sin el Content-Type esperado (application/json).
     *
     * @param ex excepcion de tipo de contenido no soportado.
     * @return respuesta 415.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleContentType(HttpMediaTypeNotSupportedException ex) {
        return construirRespuesta(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "La petición debe enviarse con el encabezado Content-Type: application/json.");
    }

    /**
     * Maneja cualquier otro error inesperado. El detalle tecnico se escribe
     * en la consola del servidor, pero al usuario solo se le muestra un
     * mensaje generico.
     *
     * @param ex excepcion no controlada.
     * @return respuesta 500 con mensaje generico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        LOG.error("Error inesperado en el servidor", ex);
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado en el servidor. Intente nuevamente.");
    }

    /**
     * Construye la respuesta de error con el formato estandar del proyecto.
     *
     * @param status codigo HTTP.
     * @param mensaje mensaje para el usuario.
     * @return respuesta HTTP con el cuerpo ErrorResponse.
     */
    private ResponseEntity<ErrorResponse> construirRespuesta(HttpStatus status, String mensaje) {
        ErrorResponse error = new ErrorResponse(status.value(), status.getReasonPhrase(), mensaje);
        return ResponseEntity.status(status).body(error);
    }
}
