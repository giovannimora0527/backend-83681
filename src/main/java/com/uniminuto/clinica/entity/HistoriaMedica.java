package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla "historia_medica" (historia clinica).
 * Cada historia corresponde a UNA consulta: se genera a partir de una cita
 * ATENDIDA (relacion 1 a 1 con la cita) y puede tener muchas anotaciones.
 */
@Entity
@Table(name = "historia_medica")
@Data
public class HistoriaMedica {

    /**
     * Identificador unico de la historia clinica.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "historia_id")
    private Long id;

    /**
     * Mascota (paciente) a la que pertenece la historia.
     */
    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    /**
     * Cita atendida que origino la historia. Una cita solo puede tener una historia.
     */
    @OneToOne
    @JoinColumn(name = "cita_id", nullable = false, unique = true)
    private CitaMedica cita;

    /**
     * Veterinario que realizo la consulta.
     */
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    /**
     * Fecha y hora de la consulta.
     */
    @Column(name = "fecha_consulta", nullable = false)
    private LocalDateTime fechaConsulta;

    /**
     * Motivo de la consulta.
     */
    @Column(name = "motivo", length = 200)
    private String motivo;

    /**
     * Diagnostico emitido por el veterinario.
     */
    @Column(name = "diagnostico", columnDefinition = "TEXT")
    private String diagnostico;

    /**
     * Observaciones generales de la consulta.
     */
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    /**
     * Estado de la historia: ABIERTA (acepta anotaciones) o CERRADA.
     */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;
}
