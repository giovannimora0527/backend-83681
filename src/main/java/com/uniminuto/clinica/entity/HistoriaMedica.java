package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Representa la entidad historia_medica en la base de datos.
 * Contiene la referencia al paciente y la fecha de creación de la historia.
 */
@Entity
@Data
@Table(name = "historia_medica")
public class HistoriaMedica {

    /** Identificador único de la historia médica (PK auto-generado). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Identificador del paciente asociado a esta historia. */
    @Column(name = "paciente_id")
    private Integer pacienteId;

    /** Fecha y hora en que se creó la historia. */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
