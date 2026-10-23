package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos que envia el frontend para crear una historia clinica.
 */
@Data
public class HistoriaClinicaRq {

    /**
     * Id del cliente dueño de la mascota.
     */
    private Long clienteId;

    /**
     * Id de la mascota (paciente).
     */
    private Long mascotaId;

    /**
     * Id de la cita atendida que origina la historia.
     */
    private Long citaId;

    /**
     * Id del veterinario que realizo la consulta.
     */
    private Long medicoId;

    /**
     * Fecha y hora de la consulta. Si no se envia se usa la fecha actual.
     */
    private LocalDateTime fechaHora;

    /**
     * Motivo de la consulta (obligatorio).
     */
    private String motivo;

    /**
     * Diagnostico (obligatorio).
     */
    private String diagnostico;

    /**
     * Observaciones (opcional).
     */
    private String observaciones;
}
