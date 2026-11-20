package com.uniminuto.clinica.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name = "tarifa_servicio") @Data
public class TarifaServicio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "especializacion_id") private Especializacion especializacion;
    @Column(name = "nombre", nullable = false, length = 160) private String nombre;
    @Column(name = "descripcion", length = 500) private String descripcion;
    @Column(name = "precio", nullable = false, precision = 12, scale = 2) private BigDecimal precio;
    @Column(name = "iva_porcentaje", nullable = false, precision = 5, scale = 2) private BigDecimal ivaPorcentaje;
    @Column(name = "activo", nullable = false) private Boolean activo;
    @Column(name = "vigente_desde", nullable = false) private LocalDateTime vigenteDesde;
    @Column(name = "vigente_hasta") private LocalDateTime vigenteHasta;
}
