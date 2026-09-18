package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Objeto de solicitud (request) para crear o actualizar una cita.
 */
@Data
public class CitaRq {

    /**
     * Identificador de la cita. Requerido únicamente al actualizar.
     */
    private Long citaId;

    /**
     * Fecha y hora en la que está programada la cita.
     */
    private LocalDateTime fechaCita;

    /**
     * Motivo de la cita.
     */
    private String motivo;

    /**
     * Estado de la cita.
     */
    private String estado;

    /**
     * Identificador de la mascota asociada a la cita.
     */
    private Long mascotaId;

    /**
     * Identificador del médico que atiende la cita.
     */
    private Long medicoId;
}
