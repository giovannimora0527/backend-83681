package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "factura") @Data
public class Factura {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cita_id", nullable = false) private Cita cita;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "usuario_creacion_id", nullable = false) private Usuario usuarioCreacion;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal subtotal;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal descuento;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal impuesto;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal total;
    @Column(nullable = false, length = 20) private String estado;
    @Column(name = "fecha_creacion", nullable = false) private LocalDateTime fechaCreacion;
    @Column(name = "fecha_anulacion") private LocalDateTime fechaAnulacion;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "usuario_anulacion_id") private Usuario usuarioAnulacion;
    @Column(name = "motivo_anulacion", length = 500) private String motivoAnulacion;
}
