package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import java.time.LocalDateTime;

/** Representa el encabezado de una fórmula médica almacenado en la base clínica. */
@Entity
@Table(name = "formula_medica")
@Data
public class FormulaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "numero_formula", nullable = false, length = 20)
    private String numeroFormula;

    @ManyToOne
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;

    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacionRegistro;

    @Column(name = "fecha_actualizacion_registro")
    private LocalDateTime fechaActualizacionRegistro;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "VIGENTE";

    @Transient
    private Medicamento medicamento;

    @Transient
    private String dosis;

    @Transient
    private String frecuencia;

    @Transient
    private String duracion;

    @Transient
    private Integer cantidadPrescrita;

    @Transient
    private Integer cantidadDispensada = 0;

    @Transient
    private String estadoDispensacion = "PENDIENTE";
}
