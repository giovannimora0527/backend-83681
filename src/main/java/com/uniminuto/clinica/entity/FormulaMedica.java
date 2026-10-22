package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fórmula médica. Pertenece a una mascota y a una cita (consulta) de esa misma mascota,
 * y la emite un veterinario. El medicamento es una llave a la tabla medicamento (id_medicamento), y la
 * historia clínica es opcional (id_historia), porque la fórmula también puede ligarse solo a la consulta.
 */
@Entity
@Data
@Table(name = "formula_medica")
public class FormulaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @ManyToOne
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @ManyToOne
    @JoinColumn(name = "id_medicamento", nullable = false)
    private Medicamento medicamento;

    @ManyToOne
    @JoinColumn(name = "id_historia")
    private HistoriaMedica historia;

    @Column(name = "dosis", nullable = false, length = 100)
    private String dosis;

    @Column(name = "frecuencia", nullable = false, length = 100)
    private String frecuencia;

    @Column(name = "duracion_dias", nullable = false)
    private Integer duracionDias;

    @Column(name = "indicaciones", nullable = false, columnDefinition = "TEXT")
    private String indicaciones;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacionRegistro;

    @Column(name = "fecha_actualizacion_registro")
    private LocalDateTime fechaActualizacionRegistro;

    @PrePersist
    public void prePersist() {
        if (fechaCreacionRegistro == null) {
            fechaCreacionRegistro = LocalDateTime.now();
        }
        fechaActualizacionRegistro = fechaCreacionRegistro;
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacionRegistro = LocalDateTime.now();
    }
}
