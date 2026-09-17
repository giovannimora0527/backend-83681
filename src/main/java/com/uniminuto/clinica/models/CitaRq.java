package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data

public class CitaRq {

    /**
     * Identificador único de la cita.
     */
    private Long id;

    /**
     * Identificador del cliente asociado a la cita.
     */
    private Long clienteId;

    /**
     * Identificador del médico asignado a la cita.
     */
    private Long medicoId;

    /**
     * Identificador de la mascota para la cual se agenda la cita.
     */
    private Long mascotaId;

    /**
     * Fecha y hora en la que se registra la cita.
     */
    private LocalDateTime fechaHora;

    /**
     * Motivo o descripción de la consulta médica.
     */
    private String motivo;

    /**
     * Estado actual de la cita (por ejemplo: pendiente, confirmada, cancelada).
     */
    private String estado;
}
