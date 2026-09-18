package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad que representa una fórmula médica del inventario de la clínica veterinaria.
 * Está mapeada directamente a la tabla "formula_medica" de la base de datos.
 */
@Entity
@Table(name = "formula_medica")
@Data
public class FormulaMedica {

    /**
     * Identificador único de la fórmula médica (llave primaria, autoincremental).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Cita en la que se generó esta fórmula médica.
     */
    @ManyToOne
    @JoinColumn(name = "cita_id")
    private Cita cita;

    /**
     * Medicamento formulado.
     */
    @ManyToOne
    @JoinColumn(name = "medicamento_id")
    private Medicamento medicamento;

    /**
     * Dosis indicada para el medicamento (ejemplo: "500 mg cada 8 horas").
     */
    @Column(name = "dosis")
    private String dosis;

    /**
     * Indicaciones adicionales sobre cómo debe tomarse el medicamento.
     */
    @Column(name = "indicaciones")
    private String indicaciones;

    /**
     * Fecha y hora en que se creó el registro de la fórmula médica.
     */
    @Column(name = "fecha_creacion_registro")
    private LocalDateTime fechaCreacionRegistro;

    /**
     * Fecha y hora de la última actualización del registro.
     * Puede ser nula si la fórmula nunca se ha modificado.
     */
    @Column(name = "fecha_actualizacion_registro")
    private LocalDateTime fechaActualizacionRegistro;
}