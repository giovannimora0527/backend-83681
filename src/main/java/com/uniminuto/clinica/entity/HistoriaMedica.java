package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad que representa la historia médica de un paciente (mascota).
 * Está mapeada directamente a la tabla "historia_medica" de la base de datos.
 * Cada historia médica puede tener varias anotaciones asociadas (tabla "anotacion_historia").
 */
@Entity
@Table(name = "historia_medica")
@Data
public class HistoriaMedica {

    /**
     * Identificador único de la historia médica (llave primaria, autoincremental).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Paciente (mascota) dueño de esta historia médica.
     */
    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Mascota paciente;

    /**
     * Fecha y hora en que se creó la historia médica.
     */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}