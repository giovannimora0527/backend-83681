package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity @Table(name="factura_detalle") @Data
public class FacturaDetalle {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="factura_id", nullable=false) private Long facturaId;
    @Column(name="tipo", nullable=false, length=20) private String tipo;
    @Column(name="descripcion", nullable=false, length=255) private String descripcion;
    @Column(name="medicamento_id") private Long medicamentoId;
    @Column(name="lote_id") private Long loteId;
    @Column(name="despacho_detalle_id", unique=true) private Long despachoDetalleId;
    @Column(name="cantidad", nullable=false, precision=12, scale=3) private BigDecimal cantidad;
    @Column(name="precio_unitario", nullable=false, precision=12, scale=2) private BigDecimal precioUnitario;
    @Column(name="impuesto_porcentaje", nullable=false, precision=5, scale=2) private BigDecimal impuestoPorcentaje;
    @Column(name="subtotal", nullable=false, precision=12, scale=2) private BigDecimal subtotal;
}
