package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "despacho_detalle") @Data
public class DespachoDetalle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "despacho_id", nullable = false) private Despacho despacho;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "formula_detalle_id", nullable = false) private FormulaDetalle formulaDetalle;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "lote_id", nullable = false) private InventarioLote lote;
    @Column(name = "cantidad", nullable = false, precision = 12, scale = 3) private BigDecimal cantidad;
    @Column(name = "creado_en", nullable = false) private LocalDateTime creadoEn;
}
