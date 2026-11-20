package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity @Table(name = "formula_detalle") @Data
public class FormulaDetalle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "atencion_id", nullable = false) private Long atencionId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;
    @Column(name = "cantidad_prescrita", nullable = false, precision = 12, scale = 3) private BigDecimal cantidadPrescrita;
    @Column(name = "dosis", nullable = false, columnDefinition = "TEXT") private String dosis;
    @Column(name = "frecuencia") private String frecuencia;
    @Column(name = "duracion") private String duracion;
    @Column(name = "via_administracion") private String viaAdministracion;
    @Column(name = "indicaciones", columnDefinition = "TEXT") private String indicaciones;
    @Column(name = "cantidad_dispensada", nullable = false, precision = 12, scale = 3) private BigDecimal cantidadDispensada = BigDecimal.ZERO;
}
