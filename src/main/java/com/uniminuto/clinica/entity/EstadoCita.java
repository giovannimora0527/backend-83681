package com.uniminuto.clinica.entity;

import com.uniminuto.clinica.exception.BadRequestException;

import java.util.EnumSet;
import java.util.Set;

/**
 * Estados posibles de una cita y las transiciones permitidas entre ellos.
 *
 * <pre>
 * PENDIENTE  -> PROGRAMADA, CONFIRMADA, ATENDIDA, CANCELADA
 * PROGRAMADA -> CONFIRMADA, ATENDIDA, CANCELADA
 * CONFIRMADA -> ATENDIDA, CANCELADA
 * ATENDIDA   -> (ninguno: es información histórica)
 * CANCELADA  -> (ninguno: solo se reactiva con la operación explícita "reactivar")
 * </pre>
 */
public enum EstadoCita {

    /** Cita registrada a la espera de ser programada o confirmada. */
    PENDIENTE,
    /** Cita agendada por la clínica. */
    PROGRAMADA,
    /** Cita confirmada por el cliente. */
    CONFIRMADA,
    /** Cita en la que la mascota ya fue atendida. */
    ATENDIDA,
    /** Cita cancelada. */
    CANCELADA;

    /**
     * Estados con los que se puede crear una cita nueva. Una cita nueva
     * siempre es futura, por eso no puede nacer atendida ni cancelada.
     */
    public static final Set<EstadoCita> ESTADOS_INICIALES = EnumSet.of(PENDIENTE, PROGRAMADA, CONFIRMADA);

    /**
     * Indica si la información de la cita (fecha, hora, veterinario, motivo)
     * todavía puede modificarse. Las citas atendidas o canceladas no.
     *
     * @return true si la cita está activa y puede editarse.
     */
    public boolean esModificable() {
        return this != ATENDIDA && this != CANCELADA;
    }

    /**
     * Indica si desde el estado actual se puede pasar al estado destino
     * mediante un cambio de estado normal.
     *
     * @param destino estado al que se quiere cambiar.
     * @return true si la transición está permitida.
     */
    public boolean puedeCambiarA(EstadoCita destino) {
        return switch (this) {
            case PENDIENTE -> EnumSet.of(PROGRAMADA, CONFIRMADA, ATENDIDA, CANCELADA).contains(destino);
            case PROGRAMADA -> EnumSet.of(CONFIRMADA, ATENDIDA, CANCELADA).contains(destino);
            case CONFIRMADA -> EnumSet.of(ATENDIDA, CANCELADA).contains(destino);
            case ATENDIDA, CANCELADA -> false;
        };
    }

    /**
     * Convierte un texto en un estado de cita, sin distinguir mayúsculas.
     *
     * @param valor texto recibido (por ejemplo "pendiente").
     * @return estado correspondiente.
     * @throws BadRequestException si el texto está vacío o no es un estado válido.
     */
    public static EstadoCita desdeTexto(String valor) throws BadRequestException {
        if (valor == null || valor.isBlank()) {
            throw new BadRequestException("El estado de la cita es obligatorio.");
        }
        try {
            return EstadoCita.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("El estado '" + valor
                    + "' no es válido. Valores permitidos: PENDIENTE, PROGRAMADA, CONFIRMADA, ATENDIDA, CANCELADA.");
        }
    }
}
