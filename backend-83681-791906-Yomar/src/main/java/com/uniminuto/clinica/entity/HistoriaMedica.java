package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "historia_medica")
public class HistoriaMedica {

    /**
     * Identificador único de la historia médica.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Paciente (mascota) al que pertenece esta historia médica.
     */
    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Mascota paciente;

    /**
     * Fecha de creación de la historia médica.
     */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}