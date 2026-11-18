package com.uniminuto.clinica.exception;

import com.uniminuto.clinica.exception.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

/**
 * Unifica las respuestas ante solicitudes inválidas y problemas de permisos. Indica el parámetro
 * incorrecto o faltante cuando corresponde.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(403, "FORBIDDEN", ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex) {

        ErrorResponse error = new ErrorResponse(
                ex.getStatus().value(),
                "BAD REQUEST",
                ex.getMessage()
        );

        return ResponseEntity
                .status(ex.getStatus())
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL SERVER_ERROR",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        return handleBadRequest(new BadRequestException("Falta el parámetro " + ex.getParameterName() + "."));
    }

    /** Devuelve un mensaje con el nombre del parámetro que no se pudo interpretar. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameter(MethodArgumentTypeMismatchException ex) {
        String mensaje = "El parámetro " + ex.getName() + " tiene un valor inválido.";
        if (LocalDateTime.class.equals(ex.getRequiredType())) {
            mensaje = "El parámetro " + ex.getName() + " debe ser una fecha y hora ISO 8601, por ejemplo 2026-11-23T09:00:00.";
        }
        return handleBadRequest(new BadRequestException(mensaje));
    }

    /** Indica que el cuerpo de la solicitud o alguna de sus fechas tiene un formato incorrecto. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBody(HttpMessageNotReadableException ex) {
        return handleBadRequest(new BadRequestException("Envía un cuerpo JSON válido. Las fechas y horas deben usar ISO 8601."));
    }
}
