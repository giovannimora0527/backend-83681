package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "pago_factura") @Data
public class PagoFactura {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "factura_id", nullable = false) private Factura factura;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "usuario_id", nullable = false) private Usuario usuario;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal monto;
    @Column(nullable = false, length = 30) private String metodo;
    @Column(length = 120) private String referencia;
    @Column(nullable = false) private LocalDateTime fecha;
}
