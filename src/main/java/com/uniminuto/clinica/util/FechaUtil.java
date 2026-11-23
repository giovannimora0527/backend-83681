package com.uniminuto.clinica.util;

import com.uniminuto.clinica.exception.BadRequestException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Utilidades para convertir fechas recibidas en texto.
 * Acepta yyyy-MM-dd, yyyy-MM-ddTHH:mm:ss y yyyy-MM-dd HH:mm:ss.
 */
public final class FechaUtil {

    private FechaUtil() {
    }

    /**
     * Convierte un texto a LocalDateTime.
     *
     * @param valor         texto con la fecha.
     * @param esFechaFinal  si es solo fecha, true usa el final del día; false usa el inicio del día.
     * @return fecha convertida.
     * @throws BadRequestException si el texto es vacío o tiene formato inválido.
     */
    public static LocalDateTime parsearFecha(String valor, boolean esFechaFinal) throws BadRequestException {
        if (valor == null || valor.isBlank()) {
            throw new BadRequestException("La fecha es obligatoria");
        }
        String texto = valor.trim().replace(' ', 'T');
        try {
            return LocalDateTime.parse(texto);
        } catch (DateTimeParseException ignorado) {
            // No es fecha y hora; se intenta como solo fecha.
        }
        try {
            LocalDate fecha = LocalDate.parse(texto);
            return esFechaFinal ? fecha.atTime(LocalTime.MAX) : fecha.atStartOfDay();
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Formato de fecha inválido. Use yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss");
        }
    }
}
