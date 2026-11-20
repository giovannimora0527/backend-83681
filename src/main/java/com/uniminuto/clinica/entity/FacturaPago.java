package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="factura_pago") @Data
public class FacturaPago {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="factura_id", nullable=false) private Long facturaId;
    @Column(name="monto", nullable=false, precision=12, scale=2) private BigDecimal monto;
    @Column(name="medio", nullable=false, length=40) private String medio;
    @Column(name="referencia", length=120) private String referencia;
    @Column(name="recibido_por") private Long recibidoPor;
    @Column(name="pagado_en", nullable=false) private LocalDateTime pagadoEn;
}
