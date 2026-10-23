package com.uniminuto.clinica.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Cita médica agendada para una mascota con un veterinario.
 * El cliente de la cita se obtiene a través de la mascota (Cliente 1:N Mascota 1:N Cita).
 */
@Entity
@Data
@Table(name = "citas")
public class Cita {

    /** Identificador único de la cita. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Mascota que será atendida. */
    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    /** Veterinario asignado. */
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    /** Fecha y hora de la cita. */
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    /** Motivo de la consulta. */
    @Column(name = "motivo", nullable = false, length = 200)
    private String motivo;

    /** Observaciones adicionales. */
    @Column(name = "observaciones", length = 255)
    private String observaciones;

    /** Estado de la cita (PENDIENTE, CONFIRMADA, PROGRAMADA, ATENDIDA, CANCELADA). */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;
}
