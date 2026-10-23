package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla "formula_medica".
 * Una formula se emite para una mascota, en una cita (consulta) atendida,
 * por un veterinario. Nunca se borra fisicamente: se inactiva (activo = false).
 */
@Entity
@Table(name = "formula_medica")
@Data
public class FormulaMedica {

    /**
     * Identificador unico de la formula.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "formula_id")
    private Long id;

    /**
     * Mascota a la que se le formula el medicamento.
     */
    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    /**
     * Veterinario que emite la formula.
     */
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    /**
     * Cita (consulta) en la que se emitio la formula.
     */
    @ManyToOne
    @JoinColumn(name = "cita_id", nullable = false)
    private CitaMedica cita;

    /**
     * Nombre del medicamento.
     */
    @Column(name = "medicamento", nullable = false, length = 150)
    private String medicamento;

    /**
     * Cantidad de la dosis (numero mayor que cero, ej: 250).
     */
    @Column(name = "dosis_cantidad", nullable = false, precision = 10, scale = 2)
    private BigDecimal dosisCantidad;

    /**
     * Unidad de la dosis (mg, ml, mg/kg, tableta, gotas, UI, g).
     */
    @Column(name = "dosis_unidad", nullable = false, length = 20)
    private String dosisUnidad;

    /**
     * Frecuencia de administracion (ej: "Cada 8 horas").
     */
    @Column(name = "frecuencia", nullable = false, length = 100)
    private String frecuencia;

    /**
     * Duracion del tratamiento en dias (entre 1 y 365).
     */
    @Column(name = "duracion_dias", nullable = false)
    private Integer duracionDias;

    /**
     * Indicaciones para el cliente (como administrar el medicamento).
     */
    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;

    /**
     * Fecha de emision de la formula. No puede ser anterior a la cita.
     */
    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    /**
     * Fecha en que se registro la formula en el sistema.
     */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de la ultima modificacion.
     */
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    /**
     * Indica si la formula esta vigente (true) o inactivada (false).
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo;
}
