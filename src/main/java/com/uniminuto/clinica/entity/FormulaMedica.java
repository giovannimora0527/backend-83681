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
 * Entidad que representa una fórmula médica del inventario de la
 * Clínica Veterinaria (medicamento formulado a una mascota por un médico).
 */
@Entity
@Table(name = "formula_medica")
@Data
public class FormulaMedica {

    /**
     * Identificador único de la fórmula médica.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "formula_id")
    private Long formulaId;

    /**
     * Nombre del medicamento formulado.
     */
    @Column(name = "medicamento", nullable = false, length = 150)
    private String medicamento;

    /**
     * Dosis indicada para el medicamento (ej: "5 mg cada 12 horas").
     */
    @Column(name = "dosis", nullable = false, length = 100)
    private String dosis;

    /**
     * Indicaciones adicionales sobre el suministro del medicamento.
     */
    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;

    /**
     * Cantidad de unidades disponibles en el inventario para esta fórmula.
     */
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    /**
     * Fecha en la que se creó/registró la fórmula médica en el inventario.
     */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de la última modificación de la fórmula médica.
     */
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    /**
     * Mascota a la que se le formula el medicamento.
     */
    @ManyToOne
    @JoinColumn(name = "mascota_id")
    private Mascota mascota;

    /**
     * Médico que formula el medicamento.
     */
    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;
}
