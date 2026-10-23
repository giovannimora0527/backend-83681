package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos que recibe el backend para crear o editar una cita.
 */
@Data
public class CitaRq {
    /** Cliente dueño de la mascota. */
    private Long clienteId;
    /** Mascota que será atendida (debe pertenecer al cliente). */
    private Long mascotaId;
    /** Veterinario asignado. */
    private Long medicoId;
    /** Fecha y hora de la cita. */
    private LocalDateTime fechaHora;
    /** Motivo de la consulta. */
    private String motivo;
    /** Estado de la cita. */
    private String estado;
    /** Observaciones adicionales. */
    private String observaciones;
}
