package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "anotacion_historia")
public class AnotacionHistoria {

    /**
     * Identificador único de la anotación.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Historia médica a la que pertenece esta anotación.
     */
    @ManyToOne
    @JoinColumn(name = "historia_id")
    private HistoriaMedica historia;

    /**
     * Médico que registró la anotación.
     */
    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;

    /**
     * Fecha en que se registró la anotación.
     */
    @Column(name = "fecha")
    private LocalDateTime fecha;

    /**
     * Texto descriptivo de la anotación.
     */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
}