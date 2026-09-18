package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Objeto de entrada (request) para crear o actualizar una cita.
 * Cuando se usa para actualizar, el campo "id" es obligatorio.
 */
@Data
public class CitaRq {

    /**
     * Identificador de la cita. Solo se usa (y es obligatorio) al actualizar.
     */
    private Long id;

    /**
     * Identificador del cliente dueño de la mascota.
     */
    private Long clienteId;

    /**
     * Identificador de la mascota que asiste a la cita.
     */
    private Long mascotaId;

    /**
     * Identificador del médico que atiende la cita.
     */
    private Long medicoId;

    /**
     * Fecha y hora programada de la cita.
     */
    private LocalDateTime fechaHora;

    /**
     * Estado de la cita (ejemplo: "programada", "cancelada", "atendida").
     */
    private String estado;

    /**
     * Motivo de la consulta. Es opcional.
     */
    private String motivo;
}