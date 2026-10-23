package com.uniminuto.clinica.util;

import java.util.Set;

/**
 * Constantes de los estados y valores permitidos del dominio (citas, historias y fórmulas).
 */
public final class Estados {

    /** Cita creada, a la espera de confirmación. */
    public static final String PENDIENTE = "PENDIENTE";
    /** Cita confirmada por el cliente. */
    public static final String CONFIRMADA = "CONFIRMADA";
    /** Cita programada en agenda. */
    public static final String PROGRAMADA = "PROGRAMADA";
    /** Cita ya realizada (puede generar historia clínica). */
    public static final String ATENDIDA = "ATENDIDA";
    /** Cita cancelada (no se modifica; solo se reactiva de forma explícita). */
    public static final String CANCELADA = "CANCELADA";

    /** Estados con los que puede crearse una cita nueva. */
    public static final Set<String> CITA_NUEVA = Set.of(PENDIENTE, CONFIRMADA, PROGRAMADA);
    /** Todos los estados válidos de una cita. */
    public static final Set<String> CITA_VALIDOS = Set.of(PENDIENTE, CONFIRMADA, PROGRAMADA, ATENDIDA, CANCELADA);

    /** Historia clínica abierta: admite anotaciones. */
    public static final String ABIERTA = "ABIERTA";
    /** Historia clínica cerrada: solo consulta. */
    public static final String CERRADA = "CERRADA";
    /** Estados válidos de una historia clínica. */
    public static final Set<String> HISTORIA_VALIDOS = Set.of(ABIERTA, CERRADA);

    /** Fórmula vigente. */
    public static final String ACTIVA = "ACTIVA";
    /** Fórmula inactivada (reemplaza a la eliminación física). */
    public static final String INACTIVA = "INACTIVA";

    /** Unidades permitidas para la dosis de un medicamento. */
    public static final Set<String> UNIDADES_DOSIS = Set.of("mg", "g", "ml", "gotas", "tabletas", "UI");

    private Estados() {
        // Clase de constantes: no se instancia.
    }
}
