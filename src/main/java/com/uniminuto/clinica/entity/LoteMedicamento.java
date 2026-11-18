package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "lote_medicamento")
@Data
public class LoteMedicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(name = "numero_lote", nullable = false, length = 50)
    private String numeroLote;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_compra_lote", nullable = false)
    private Double precioCompraLote;

    @Column(name = "fecha_ingreso_lote", nullable = false)
    private LocalDate fechaIngresoLote;

    @Column(name = "fecha_vencimiento_lote", nullable = false)
    private LocalDate fechaVencimientoLote;
}
