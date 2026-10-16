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

    @ManyToOne
    @JoinColumn(name = "historia_id", nullable = false)
    private HistoriaMedica historia;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @Column(name = "fecha_hora", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaHora;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "diagnostico_evolucion", columnDefinition = "TEXT", nullable = false)
    private String diagnosticoEvolucion;
}
