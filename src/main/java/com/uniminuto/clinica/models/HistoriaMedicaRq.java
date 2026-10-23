package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos que recibe el backend para registrar una historia clínica.
 */
@Data
public class HistoriaMedicaRq {
    /** Cliente dueño de la mascota. */
    private Long clienteId;
    /** Mascota de la historia (debe pertenecer al cliente). */
    private Long mascotaId;
    /** Cita atendida que origina la historia. */
    private Long citaId;
    /** Veterinario que realizó la consulta. */
    private Long medicoId;
    /** Fecha y hora de la consulta. */
    private LocalDateTime fechaHora;
    /** Motivo de la consulta. */
    private String motivoConsulta;
    /** Diagnóstico. */
    private String diagnostico;
    /** Observaciones generales. */
    private String observaciones;
    /** Estado inicial (ABIERTA o CERRADA). */
    private String estado;
}
