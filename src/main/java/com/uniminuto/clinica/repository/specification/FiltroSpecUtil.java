package com.uniminuto.clinica.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Funciones comunes para construir los filtros dinámicos (Specifications)
 * de los listados paginados.
 */
public final class FiltroSpecUtil {

    /**
     * Constructor privado: clase de utilidades estáticas.
     */
    private FiltroSpecUtil() {
    }

    /**
     * Construye la expresión "nombres apellidos" en minúsculas de una persona
     * (cliente o veterinario), para poder buscar por nombre completo.
     *
     * @param cb      constructor de criterios.
     * @param persona ruta a la entidad persona.
     * @return expresión con el nombre completo en minúsculas.
     */
    public static Expression<String> nombreCompleto(CriteriaBuilder cb, Path<?> persona) {
        return cb.lower(cb.concat(cb.concat(persona.get("nombres"), " "), persona.<String>get("apellidos")));
    }

    /**
     * Agrega a la lista de predicados el filtro por rango de fechas sobre una
     * columna de tipo fecha-hora. Los días indicados se incluyen completos.
     *
     * @param predicados lista donde se agregan los filtros.
     * @param cb         constructor de criterios.
     * @param columna    columna de tipo {@link LocalDateTime}.
     * @param desde      fecha inicial (opcional).
     * @param hasta      fecha final (opcional).
     */
    public static void rangoFechaHora(List<Predicate> predicados, CriteriaBuilder cb, Path<LocalDateTime> columna,
                                      LocalDate desde, LocalDate hasta) {
        if (desde != null) {
            predicados.add(cb.greaterThanOrEqualTo(columna, desde.atStartOfDay()));
        }
        if (hasta != null) {
            predicados.add(cb.lessThanOrEqualTo(columna, hasta.atTime(LocalTime.MAX)));
        }
    }

    /**
     * Agrega a la lista de predicados el filtro por rango sobre una columna de
     * tipo fecha.
     *
     * @param predicados lista donde se agregan los filtros.
     * @param cb         constructor de criterios.
     * @param columna    columna de tipo {@link LocalDate}.
     * @param desde      fecha inicial (opcional).
     * @param hasta      fecha final (opcional).
     */
    public static void rangoFecha(List<Predicate> predicados, CriteriaBuilder cb, Path<LocalDate> columna,
                                  LocalDate desde, LocalDate hasta) {
        if (desde != null) {
            predicados.add(cb.greaterThanOrEqualTo(columna, desde));
        }
        if (hasta != null) {
            predicados.add(cb.lessThanOrEqualTo(columna, hasta));
        }
    }
}
