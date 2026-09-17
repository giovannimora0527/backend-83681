package com.uniminuto.clinica.util;

import com.uniminuto.clinica.exception.BadRequestException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Utilidad para validar y transformar los rangos de fechas que reciben los
 * servicios de filtrado. Evita repetir la misma logica en cada servicio.
 */
public final class FechaUtil {

    /**
     * Constructor privado para impedir que la clase de utilidad sea instanciada.
     */
    private FechaUtil() {
    }

    /**
     * Valida que el rango de fechas recibido sea coherente.
     *
     * @param fechaInicial Fecha inicial del rango.
     * @param fechaFinal Fecha final del rango.
     * @throws BadRequestException Si alguna fecha es nula o si la fecha inicial
     *                             es posterior a la fecha final.
     */
    public static void validarRango(LocalDate fechaInicial, LocalDate fechaFinal)
            throws BadRequestException {
        if (fechaInicial == null || fechaFinal == null) {
            throw new BadRequestException(
                    "La fecha inicial y la fecha final son obligatorias.");
        }
        if (fechaInicial.isAfter(fechaFinal)) {
            throw new BadRequestException(
                    "La fecha inicial no puede ser posterior a la fecha final.");
        }
    }

    /**
     * Convierte una fecha en el primer instante del dia (00:00:00) para usarla
     * como limite inferior de una consulta.
     *
     * @param fecha Fecha a convertir.
     * @return Fecha y hora correspondiente al inicio del dia.
     */
    public static LocalDateTime inicioDelDia(LocalDate fecha) {
        return fecha.atStartOfDay();
    }

    /**
     * Convierte una fecha en el ultimo instante del dia (23:59:59) para usarla
     * como limite superior de una consulta e incluir todo el dia final.
     *
     * @param fecha Fecha a convertir.
     * @return Fecha y hora correspondiente al final del dia.
     */
    public static LocalDateTime finDelDia(LocalDate fecha) {
        return fecha.atTime(LocalTime.MAX);
    }
}
