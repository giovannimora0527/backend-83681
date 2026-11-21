package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lote_medicamento", uniqueConstraints =
        @UniqueConstraint(name = "uk_lote", columnNames = {"medicamento_id", "numero_lote"}))
@Data
public class LoteMedicamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(name = "numero_lote", nullable = false, length = 50)
    private String numeroLote;

    @Column(name = "fecha_vence", nullable = false)
    private LocalDate fechaVence;

    @Column(name = "precio_compra", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioCompra;

    @Column(name = "cantidad_inicial", nullable = false)
    private Integer cantidadInicial;

    @Column(name = "cantidad_disponible", nullable = false)
    private Integer cantidadDisponible;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;
}
