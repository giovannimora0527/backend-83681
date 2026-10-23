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
 * Anotación (evolución) registrada dentro de una historia clínica.
 * Una historia clínica puede tener muchas anotaciones.
 */
@Entity
@Table(name = "anotacion_historia")
@Data
public class AnotacionHistoria {

    /**
     * Identificador de la anotación.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "anotacion_id")
    private Long anotacionId;

    /**
     * Historia clínica a la que pertenece la anotación.
     */
    @ManyToOne
    @JoinColumn(name = "historia_id", nullable = false)
    private HistoriaMedica historiaMedica;

    /**
     * Veterinario que registra la anotación.
     */
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    /**
     * Fecha y hora de la anotación.
     */
    @Column(name = "fecha_anotacion", nullable = false)
    private LocalDateTime fechaAnotacion;

    /**
     * Descripción de lo observado o realizado.
     */
    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Observación adicional del veterinario.
     */
    @Column(name = "observacion", nullable = false, columnDefinition = "TEXT")
    private String observacion;

    /**
     * Diagnóstico o evolución del paciente.
     */
    @Column(name = "diagnostico_evolucion", nullable = false, columnDefinition = "TEXT")
    private String diagnosticoEvolucion;

    /**
     * Fecha de creación del registro.
     */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
