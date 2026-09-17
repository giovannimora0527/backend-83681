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

import java.time.LocalDateTime;

/**
 * Entidad que representa una formula medica del inventario de la clinica.
 */
@Entity
@Data
@Table(name = "formula_medica")
public class FormulaMedica {

    /** Identificador unico de la formula medica. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "formula_id")
    private Long formulaId;

    /** Nombre del medicamento formulado. */
    @Column(name = "medicamento", nullable = false, length = 150)
    private String medicamento;

    /** Dosis indicada para el medicamento. */
    @Column(name = "dosis", length = 100)
    private String dosis;

    /** Cantidad de unidades disponibles en el inventario. */
    @Column(name = "cantidad")
    private Integer cantidad;

    /** Observaciones adicionales de la formula. */
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    /** Fecha en la que se creo la formula medica. */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /** Fecha de la ultima modificacion de la formula medica. */
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    /** Mascota a la que se le formulo el medicamento. */
    @ManyToOne
    @JoinColumn(name = "mascota_id")
    private Mascota mascota;

    /** Medico que genero la formula medica. */
    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;
}
