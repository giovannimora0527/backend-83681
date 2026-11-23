package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity @Table(name = "factura_detalle") @Data
public class FacturaDetalle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "factura_id", nullable = false) private Factura factura;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "tarifa_id") private TarifaServicio tarifa;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "despacho_id") private DespachoInventario despacho;
    @Column(nullable = false, length = 255) private String descripcion;
    @Column(nullable = false) private Integer cantidad;
    @Column(name = "valor_unitario", nullable = false, precision = 12, scale = 2) private BigDecimal valorUnitario;
    @Column(name = "impuesto_porcentaje", nullable = false, precision = 5, scale = 2) private BigDecimal impuestoPorcentaje;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal descuento;
    @Column(name = "total_linea", nullable = false, precision = 12, scale = 2) private BigDecimal totalLinea;
}
