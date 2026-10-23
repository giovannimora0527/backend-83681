package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Cita médica programada para una mascota con un veterinario.
 * El cliente de la cita se obtiene a través de la mascota
 * (cita.mascota.cliente), así no se duplica la información ni puede quedar
 * una cita con un cliente distinto al dueño de la mascota.
 */
@Entity
@Table(name = "cita")
@Data
public class Cita {

    /**
     * Identificador de la cita.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cita_id")
    private Long citaId;

    /**
     * Fecha y hora de la cita.
     */
    @Column(name = "fecha_cita", nullable = false)
    private LocalDateTime fechaCita;

    /**
     * Motivo de la consulta.
     */
    @Column(name = "motivo", nullable = false, length = 255)
    private String motivo;

    /**
     * Estado actual de la cita.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoCita estado;

    /**
     * Fecha de creación del registro.
     */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de la última modificación del registro.
     */
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    /**
     * Mascota que asiste a la cita.
     */
    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    /**
     * Veterinario que atiende la cita.
     */
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;
}
