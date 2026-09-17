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
 * Entidad que representa una cita agendada en la clinica veterinaria.
 */
@Entity
@Data
@Table(name = "cita")
public class Cita {

    /** Identificador unico de la cita. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cita_id")
    private Long citaId;

    /** Fecha y hora en la que se atiende la cita. */
    @Column(name = "fecha_cita", nullable = false)
    private LocalDateTime fechaCita;

    /** Motivo por el cual se agenda la cita. */
    @Column(name = "motivo", length = 250)
    private String motivo;

    /** Estado actual de la cita (PENDIENTE, ATENDIDA, CANCELADA). */
    @Column(name = "estado", length = 20)
    private String estado;

    /** Fecha en la que se registro la cita en el sistema. */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /** Fecha de la ultima modificacion de la cita. */
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    /** Mascota que asiste a la cita. */
    @ManyToOne
    @JoinColumn(name = "mascota_id")
    private Mascota mascota;

    /** Medico asignado para atender la cita. */
    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;
}
