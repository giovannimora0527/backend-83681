package com.uniminuto.clinica.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Fórmula médica emitida por un veterinario para una mascota, asociada a una historia clínica.
 * No se elimina físicamente: se inactiva para conservar el historial clínico.
 */
@Entity
@Data
@Table(name = "formula_medica")
public class FormulaMedica {

    /** Identificador único de la fórmula. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "formula_id")
    private Long id;

    /** Mascota a la que se formula. */
    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    /** Historia clínica (consulta) a la que pertenece la fórmula. */
    @ManyToOne
    @JoinColumn(name = "historia_medica_id")
    private HistoriaMedica historiaMedica;

    /** Veterinario que emite la fórmula. */
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    /** Medicamento formulado. */
    @ManyToOne
    @JoinColumn(name = "medicamento_id")
    private Medicamento medicamento;

    /** Fecha en que el sistema registró la fórmula (orden inicial del listado). */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    /** Fecha de emisión indicada por el veterinario. */
    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision;

    /** Cantidad de medicamento por toma. */
    @Column(name = "dosis", precision = 10, scale = 2)
    private BigDecimal dosis;

    /** Unidad de la dosis (mg, ml, tabletas...). */
    @Column(name = "unidad_dosis", length = 20)
    private String unidadDosis;

    /** Cada cuántas horas se administra. */
    @Column(name = "frecuencia_horas")
    private Integer frecuenciaHoras;

    /** Duración del tratamiento en días. */
    @Column(name = "duracion_dias")
    private Integer duracionDias;

    /** Indicaciones para el cliente. */
    @Column(name = "indicaciones", length = 500)
    private String indicaciones;

    /** Estado de la fórmula (ACTIVA o INACTIVA). */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;
}
