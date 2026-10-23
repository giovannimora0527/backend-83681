package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos para crear o editar una cita.
 */
@Data
public class CitaRq {

    /**
     * Cliente dueño de la mascota.
     */
    private Long clienteId;

    /**
     * Mascota que asistirá a la cita. Debe pertenecer al cliente.
     */
    private Long mascotaId;

    /**
     * Veterinario que atenderá la cita.
     */
    private Long medicoId;

    /**
     * Fecha y hora de la cita (formato yyyy-MM-ddTHH:mm:ss).
     */
    private LocalDateTime fechaCita;

    /**
     * Motivo de la consulta.
     */
    private String motivo;

    /**
     * Estado inicial de la cita. Solo se usa al crear; para cambiarlo después
     * se usa el servicio de cambio de estado.
     */
    private String estado;
}
