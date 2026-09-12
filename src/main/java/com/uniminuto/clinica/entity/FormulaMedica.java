package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "formula_medica")
public class FormulaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "cita_id")
    private Long citaId;

    @Column(name = "medicamento_id")
    private Long medicamentoId;

    @Column(name = "dosis", columnDefinition = "TEXT")
    private String dosis;

    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;

    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacionRegistro;

    @Column(name = "fecha_actualizacion_registro")
    private LocalDateTime fechaActualizacionRegistro;

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacionRegistro == null) {
            this.fechaCreacionRegistro = LocalDateTime.now();
        }
        if (this.fechaActualizacionRegistro == null) {
            this.fechaActualizacionRegistro = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacionRegistro = LocalDateTime.now();
    }
}
