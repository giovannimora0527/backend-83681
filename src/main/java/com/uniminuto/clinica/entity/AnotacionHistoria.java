package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad que mapea la tabla anotacion_historia.
 * Contiene anotaciones realizadas por un médico sobre una historia médica.
 */
@Entity
@Data
@Table(name = "anotacion_historia")
public class AnotacionHistoria {

    /** Identificador único de la anotación (PK auto-generado). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Relación ManyToOne hacia la historia médica asociada (historia_id). */
    @ManyToOne
    @JoinColumn(name = "historia_id")
    private HistoriaMedica historia;

    /** Identificador del médico que realizó la anotación (medico_id). */
    @Column(name = "medico_id")
    private Integer medicoId;

    /** Texto de la anotación (columna descripcion, tipo TEXT). */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /** Fecha y hora de la anotación (columna fecha). */
    @Column(name = "fecha")
    private LocalDateTime fecha;
}
