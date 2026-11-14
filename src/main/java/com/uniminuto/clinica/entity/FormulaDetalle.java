package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Mapeo de una línea de la fórmula médica, usada para validar el despacho.
 */
@Entity
@Table(name = "formula_detalle")
@Data
public class FormulaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "formula_id", nullable = false)
    private Long formulaId;

    @Column(name = "medicamento_id", nullable = false)
    private Long medicamentoId;

    @Column(name = "cantidad_prescrita", nullable = false)
    private Integer cantidadPrescrita;

    @Column(name = "dosis", nullable = false, columnDefinition = "TEXT")
    private String dosis;

    @Column(name = "frecuencia", nullable = false, length = 100)
    private String frecuencia;

    @Column(name = "duracion_tratamiento", nullable = false, length = 50)
    private String duracionTratamiento;

    @Column(name = "via_administracion", nullable = false, length = 50)
    private String viaAdministracion;

    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;
}
