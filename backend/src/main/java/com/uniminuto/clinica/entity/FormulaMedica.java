package com.uniminuto.clinica.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Encabezado de la fórmula médica (formulación). El detalle (medicamentos prescritos) está en
 * formula_medica_detalle. La entrega/dispensación vive en las tablas despacho*.
 * Entidad de solo lectura para el historial clínico; la escritura se hace en el módulo de atención.
 */
@Entity
@Table(name = "formula_medica")
@Data
public class FormulaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    @Column(name = "medico_id", nullable = false)
    private Long medicoId;

    @Column(name = "mascota_id", nullable = false)
    private Integer mascotaId;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Column(name = "estado_despacho", nullable = false)
    private String estadoDespacho;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacionRegistro;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @Column(name = "fecha_actualizacion_registro")
    private LocalDateTime fechaActualizacionRegistro;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "formula_id", insertable = false, updatable = false)
    private List<FormulaMedicaDetalle> detalles = new ArrayList<>();
}
