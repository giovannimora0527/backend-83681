package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos que envia el frontend para crear o actualizar una cita.
 */
@Data
public class CitaRequest {

    /**
     * Id de la cita. Solo se usa al actualizar.
     */
    private Long id;

    /**
     * Id del cliente dueño de la mascota.
     */
    private Long clienteId;

    /**
     * Id de la mascota que asistira a la cita.
     */
    private Long mascotaId;

    /**
     * Id del veterinario asignado.
     */
    private Long medicoId;

    /**
     * Motivo de la consulta.
     */
    private String motivo;

    /**
     * Fecha y hora de la cita (formato yyyy-MM-ddTHH:mm:ss).
     */
    private LocalDateTime fechaHora;

    /**
     * Estado de la cita.
     */
    private String estado;
}
