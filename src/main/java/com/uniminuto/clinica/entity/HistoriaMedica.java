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
 * Entidad que representa la historia médica de una mascota en la
 * Clínica Veterinaria. Tiene una relación de uno a muchos con
 * {@link AnotacionHistoria}.
 */
@Entity
@Table(name = "historia_medica")
@Data
public class HistoriaMedica {

    /**
     * Identificador único de la historia médica.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "historia_id")
    private Long historiaId;

    /**
     * Diagnóstico registrado en la historia médica.
     */
    @Column(name = "diagnostico", columnDefinition = "TEXT")
    private String diagnostico;

    /**
     * Tratamiento indicado en la historia médica.
     */
    @Column(name = "tratamiento", columnDefinition = "TEXT")
    private String tratamiento;

    /**
     * Fecha en la que se creó la historia médica.
     */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de la última modificación de la historia médica.
     */
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    /**
     * Mascota a la que pertenece la historia médica.
     */
    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    /**
     * Médico que registra la historia médica.
     */
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;
}
