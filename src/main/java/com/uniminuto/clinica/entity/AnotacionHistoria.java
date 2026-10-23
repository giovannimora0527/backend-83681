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
 * Anotación de seguimiento asociada a una historia clínica.
 */
@Entity
@Data
@Table(name = "anotaciones_historia")
public class AnotacionHistoria {

    /** Identificador único de la anotación. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Historia clínica a la que pertenece. */
    @ManyToOne
    @JoinColumn(name = "historia_medica_id", nullable = false)
    private HistoriaMedica historiaMedica;

    /** Veterinario que escribió la anotación. */
    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;

    /** Descripción de lo realizado o evidenciado. */
    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    /** Observación adicional. */
    @Column(name = "observacion", length = 500)
    private String observacion;

    /** Diagnóstico o evolución del paciente. */
    @Column(name = "diagnostico_evolucion", length = 500)
    private String diagnosticoEvolucion;

    /** Fecha y hora de la anotación. */
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
}
