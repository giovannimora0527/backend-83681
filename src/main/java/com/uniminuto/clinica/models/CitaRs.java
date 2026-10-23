package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Cita médica con los datos del cliente, la mascota y el veterinario ya resumidos para el listado.
 */
@Data
public class CitaRs {
    /** Identificador de la cita. */
    private Long id;
    /** Fecha y hora de la cita. */
    private LocalDateTime fechaHora;
    /** Identificador del cliente. */
    private Long clienteId;
    /** Nombre completo del cliente. */
    private String clienteNombre;
    /** Identificador de la mascota. */
    private Long mascotaId;
    /** Nombre de la mascota. */
    private String mascotaNombre;
    /** Identificador del veterinario. */
    private Long medicoId;
    /** Nombre completo del veterinario. */
    private String medicoNombre;
    /** Motivo de la consulta. */
    private String motivo;
    /** Estado de la cita. */
    private String estado;
    /** Observaciones adicionales. */
    private String observaciones;
}
