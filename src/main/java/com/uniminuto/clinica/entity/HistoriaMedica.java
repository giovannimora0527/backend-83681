package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "historia_medica")
@Data
public class HistoriaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Mascota mascota;

    @ManyToOne
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @Column(name = "fecha_creacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaCreacion;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String diagnostico;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "motivo_consulta", columnDefinition = "TEXT", nullable = false)
    private String motivoConsulta;

    @Column(length = 45, nullable = false)
    private String estado;
}
