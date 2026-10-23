package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos para crear una historia clínica a partir de una cita atendida.
 */
@Data
public class HistoriaMedicaRq {

    /**
     * Cliente dueño de la mascota.
     */
    private Long clienteId;

    /**
     * Mascota atendida. Debe pertenecer al cliente.
     */
    private Long mascotaId;

    /**
     * Cita atendida que origina la historia. Debe ser de la misma mascota.
     */
    private Long citaId;

    /**
     * Veterinario que realizó la consulta.
     */
    private Long medicoId;

    /**
     * Fecha y hora de la consulta.
     */
    private LocalDateTime fechaConsulta;

    /**
     * Motivo de la consulta.
     */
    private String motivoConsulta;

    /**
     * Diagnóstico.
     */
    private String diagnostico;

    /**
     * Observaciones generales (opcional).
     */
    private String observaciones;
}
