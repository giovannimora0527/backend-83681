package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "factura") @Data
public class Factura {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="numero", nullable=false, length=40, unique=true) private String numero;
    @Column(name="atencion_id", nullable=false, unique=true) private Long atencionId;
    @Column(name="cliente_id", nullable=false) private Long clienteId;
    @Column(name="mascota_id", nullable=false) private Integer mascotaId;
    @Column(name="medico_id", nullable=false) private Long medicoId;
    @Column(name="clinica_snapshot", nullable=false, columnDefinition="JSON") private String clinicaSnapshot;
    @Column(name="propietario_snapshot", nullable=false, columnDefinition="JSON") private String propietarioSnapshot;
    @Column(name="mascota_snapshot", nullable=false, columnDefinition="JSON") private String mascotaSnapshot;
    @Column(name="medico_snapshot", nullable=false, columnDefinition="JSON") private String medicoSnapshot;
    @Column(name="subtotal", nullable=false, precision=12, scale=2) private BigDecimal subtotal;
    @Column(name="impuestos", nullable=false, precision=12, scale=2) private BigDecimal impuestos;
    @Column(name="descuentos", nullable=false, precision=12, scale=2) private BigDecimal descuentos;
    @Column(name="total", nullable=false, precision=12, scale=2) private BigDecimal total;
    @Column(name="estado", nullable=false, length=20) private String estado;
    @Column(name="emitida_en", nullable=false) private LocalDateTime emitidaEn;
    @Column(name="anulada_en") private LocalDateTime anuladaEn;
    @Column(name="motivo_anulacion", length=500) private String motivoAnulacion;
    @Column(name="motivo_descuento", length=500) private String motivoDescuento;
    @Column(name="anulada_por") private Long anuladaPor;
    @Column(name="descuento_autorizado_por") private Long descuentoAutorizadoPor;
}
