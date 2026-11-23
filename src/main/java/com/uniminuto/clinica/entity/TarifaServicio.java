package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity @Table(name = "tarifa_servicio") @Data
public class TarifaServicio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String nombre;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal valor;
    @Column(name = "impuesto_porcentaje", nullable = false, precision = 5, scale = 2) private BigDecimal impuestoPorcentaje;
    @Column(name = "vigencia_desde", nullable = false) private LocalDate vigenciaDesde;
    @Column(name = "vigencia_hasta") private LocalDate vigenciaHasta;
    @Column(nullable = false) private Boolean activo = true;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especializacion_id")
    @JsonIgnore
    private Especializacion especializacion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id")
    @JsonIgnore
    private Medico medico;
}
