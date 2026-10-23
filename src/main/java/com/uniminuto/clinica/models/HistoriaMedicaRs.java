package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Historia clínica con los datos de la mascota, el cliente y el veterinario ya resumidos.
 */
@Data
public class HistoriaMedicaRs {
    /** Identificador de la historia. */
    private Long id;
    /** Fecha y hora de la consulta. */
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
    /** Cita que originó la historia. */
    private Long citaId;
    /** Motivo de la consulta. */
    private String motivoConsulta;
    /** Diagnóstico. */
    private String diagnostico;
    /** Observaciones generales. */
    private String observaciones;
    /** Estado de la historia. */
    private String estado;
}
