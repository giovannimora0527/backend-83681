package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Objeto de entrada (DTO) usado para crear o actualizar una cita.
 */
@Data
public class CitaRq {

    /** Identificador de la cita. Solo se usa al actualizar. */
    private Long citaId;

    /** Fecha y hora en la que se programa la cita. */
    private LocalDateTime fechaCita;

    /** Motivo por el cual se agenda la cita. */
    private String motivo;

    /** Estado de la cita (PENDIENTE, ATENDIDA, CANCELADA). */
    private String estado;

    /** Identificador de la mascota que asiste a la cita. */
    private Long mascotaId;

    /** Identificador del medico que atiende la cita. */
    private Long medicoId;
}
