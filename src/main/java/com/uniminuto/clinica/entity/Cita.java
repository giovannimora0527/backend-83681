package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad que representa una cita médica de una mascota en la Clínica
 * Veterinaria.
 */
@Entity
@Table(name = "cita")
@Data
public class Cita {

    /**
     * Identificador único de la cita.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cita_id")
    private Long citaId;

    /**
     * Fecha y hora en la que está programada/se realizó la cita.
     */
    @Column(name = "fecha_cita", nullable = false)
    private LocalDateTime fechaCita;

    /**
     * Motivo de la cita.
     */
    @Column(name = "motivo", nullable = false, length = 255)
    private String motivo;

    /**
     * Estado de la cita (ej: PROGRAMADA, ATENDIDA, CANCELADA).
     */
    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    /**
     * Fecha en la que se creó el registro de la cita.
     */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de la última modificación del registro de la cita.
     */
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    /**
     * Mascota a la que corresponde la cita.
     */
    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    /**
     * Médico que atiende la cita.
     */
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;
}
