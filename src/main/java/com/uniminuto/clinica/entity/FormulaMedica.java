package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fórmula médica emitida por un veterinario dentro de una historia clínica.
 * Como siempre está ligada a información clínica, nunca se elimina
 * físicamente: se inactiva con el campo {@link #activo}.
 */
@Entity
@Table(name = "formula_medica")
@Data
public class FormulaMedica {

    /**
     * Identificador de la fórmula.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "formula_id")
    private Long formulaId;

    /**
     * Mascota a la que se le formula el medicamento.
     */
    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    /**
     * Historia clínica (consulta) en la que se emitió la fórmula.
     */
    @ManyToOne
    @JoinColumn(name = "historia_id", nullable = false)
    private HistoriaMedica historiaMedica;

    /**
     * Veterinario que emite la fórmula.
     */
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    /**
     * Fecha de emisión de la fórmula.
     */
    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    /**
     * Nombre del medicamento formulado.
     */
    @Column(name = "medicamento", nullable = false, length = 150)
    private String medicamento;

    /**
     * Cantidad de la dosis (por ejemplo 2.5).
     */
    @Column(name = "dosis", nullable = false, precision = 10, scale = 2)
    private BigDecimal dosis;

    /**
     * Unidad de la dosis (mg, ml, tableta...).
     */
    @Column(name = "unidad_dosis", nullable = false, length = 20)
    private String unidadDosis;

    /**
     * Frecuencia de administración expresada en horas (cada N horas).
     */
    @Column(name = "frecuencia_horas", nullable = false)
    private Integer frecuenciaHoras;

    /**
     * Duración del tratamiento en días.
     */
    @Column(name = "duracion_dias", nullable = false)
    private Integer duracionDias;

    /**
     * Indicaciones para el cliente.
     */
    @Column(name = "indicaciones", nullable = false, columnDefinition = "TEXT")
    private String indicaciones;

    /**
     * Indica si la fórmula está vigente. Una fórmula inactiva no se puede editar.
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo;

    /**
     * Fecha de creación del registro.
     */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de la última modificación del registro.
     */
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
}
