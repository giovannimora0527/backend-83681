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

/**
 * Entidad que representa el detalle de una fórmula médica,
 * asociando una fórmula con un medicamento, su dosis y frecuencia.
 */
@Entity
@Data
@Table(name = "formula_medica_detalle")
public class FormulaMedicaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detalle_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "formula_id", nullable = false)
    private FormulaMedica formulaMedica;

    @ManyToOne
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(name = "dosis", length = 50)
    private String dosis;

    @Column(name = "frecuencia", length = 50)
    private String frecuencia;

    @Column(name = "duracion_dias")
    private Integer duracionDias;

    // getters y setters...
}