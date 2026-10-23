package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos para reactivar una cita cancelada (operación explícita).
 */
@Data
public class ReactivarCitaRq {

    /**
     * Nueva fecha y hora de la cita. Si no se envía se conserva la original,
     * siempre que siga siendo una fecha futura.
     */
    private LocalDateTime fechaCita;
}
