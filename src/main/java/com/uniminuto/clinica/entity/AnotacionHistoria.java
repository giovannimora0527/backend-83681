package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "anotacion_historia")
@Data
public class AnotacionHistoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @JoinColumn(name = "historia_id", nullable = false)
    @ManyToOne
    private HistoriaMedica historia;

    @JoinColumn(name = "medico_id", nullable = false)
    @ManyToOne
    private Medico medico;

    @Column(name = "fecha_hora", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaHora;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "diagnostico_evolucion", columnDefinition = "TEXT")
    private String diagnostico_evolucion;
}