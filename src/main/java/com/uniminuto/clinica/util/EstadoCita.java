package com.uniminuto.clinica.util;

import java.util.List;

/**
 * Constantes con los estados posibles de una cita.
 * Se centralizan aqui para no escribir los textos "a mano" en varios lugares.
 */
public final class EstadoCita {

    /**
     * La cita fue solicitada pero aun no se ha programado formalmente.
     */
    public static final String PENDIENTE = "PENDIENTE";

    /**
     * La cita tiene fecha, hora y veterinario asignados.
     */
    public static final String PROGRAMADA = "PROGRAMADA";

    /**
     * El cliente confirmo que asistira.
     */
    public static final String CONFIRMADA = "CONFIRMADA";

    /**
     * La consulta ya se realizo. Su informacion pasa a ser historica.
     */
    public static final String ATENDIDA = "ATENDIDA";

    /**
     * La cita se cancelo. Solo puede volver a activarse con "reactivar".
     */
    public static final String CANCELADA = "CANCELADA";

    /**
     * Todos los estados validos de una cita.
     */
    public static final List<String> TODOS = List.of(PENDIENTE, PROGRAMADA, CONFIRMADA, ATENDIDA, CANCELADA);

    /**
     * Estados con los que se puede crear una cita nueva.
     * No se permite crearla ATENDIDA (aun no ha ocurrido) ni CANCELADA (no tendria sentido).
     */
    public static final List<String> INICIALES = List.of(PENDIENTE, PROGRAMADA, CONFIRMADA);

    /**
     * Constructor privado: esta clase solo contiene constantes.
     */
    private EstadoCita() {
    }
}
