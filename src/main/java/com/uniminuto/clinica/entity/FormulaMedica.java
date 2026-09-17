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

/**
 * Representa la entidad formula_medica en la base de datos.
 * Contiene información sobre la fórmula médica, incluyendo referencias a la cita y al medicamento.
 */

public class FormulaMedica {

    /**
     * Identificador único de la fórmula médica (PK auto-generado).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Referencia a la cita asociada a la fórmula médica.
     */
    @Column(name = "cita_id")
    private Long citaId;

    /**
     * Referencia al medicamento asociado a la fórmula médica.
     */
    @Column(name = "medicamento_id")
    private Long medicamentoId;

    /**
     * Dosis de la fórmula médica.
     */
    @Column(name = "dosis", columnDefinition = "TEXT")
    private String dosis;

    /**
     * Indicaciones de la fórmula médica.
     */
    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;

    /**
     * Fecha de creación del registro.
     */
    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacionRegistro;

    /**
     * Fecha de actualización del registro.
     */
    @Column(name = "fecha_actualizacion_registro")
    private LocalDateTime fechaActualizacionRegistro;

    /**
     * Método que se ejecuta antes de persistir la entidad en la base de datos.
     * Asigna automáticamente la fecha de creación y actualización si no están definidas.
     */
    @PrePersist
    public void prePersist() {
        if (this.fechaCreacionRegistro == null) {
            this.fechaCreacionRegistro = LocalDateTime.now();
        }
        if (this.fechaActualizacionRegistro == null) {
            this.fechaActualizacionRegistro = LocalDateTime.now();
        }
    }

    /**
     * Método que se ejecuta antes de actualizar la entidad en la base de datos.
     * Actualiza automáticamente la fecha de actualización del registro.
     */
    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacionRegistro = LocalDateTime.now();
    }
}
