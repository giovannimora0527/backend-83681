package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario_movimiento")
@Data
public class InventarioMovimiento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private InventarioLote lote;
    @Column(name = "tipo", nullable = false, length = 24)
    private String tipo;
    @Column(name = "cantidad", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;
    @Column(name = "referencia_tipo", length = 40)
    private String referenciaTipo;
    @Column(name = "referencia_id")
    private Long referenciaId;
    @Column(name = "motivo", length = 500)
    private String motivo;
    @Column(name = "usuario_id")
    private Long usuarioId;
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
