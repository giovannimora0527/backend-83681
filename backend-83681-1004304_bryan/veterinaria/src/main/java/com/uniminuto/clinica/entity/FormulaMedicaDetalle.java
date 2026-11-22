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

/** Medicamento prescrito dentro de una fórmula médica. */
@Entity
@Table(name = "formula_medica_detalle")
@Data
public class FormulaMedicaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "formula_id", nullable = false)
    private Long formulaId;

    @ManyToOne
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(name = "dosis", nullable = false, columnDefinition = "TEXT")
    private String dosis;

    @Column(name = "frecuencia")
    private String frecuencia;

    @Column(name = "duracion_dias")
    private Integer duracionDias;

    @Column(name = "cantidad_prescrita", nullable = false)
    private Integer cantidadPrescrita;

    @Column(name = "cantidad_dispensada", nullable = false)
    private Integer cantidadDispensada;

    @Column(name = "via_administracion")
    private String viaAdministracion;

    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;
}
