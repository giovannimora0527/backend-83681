package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento_inventario")
@Data
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @ManyToOne
    @JoinColumn(name = "lote_id")
    private InventarioLote lote;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo; // ENTRADA, SALIDA, AJUSTE, REVERSION

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDateTime fechaMovimiento;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "usuario_autorizacion")
    private Usuario usuarioAutorizacion;

    @ManyToOne
    @JoinColumn(name = "formula_id")
    private FormulaMedica formula;

    @ManyToOne
    @JoinColumn(name = "despacho_id")
    private Despacho despacho;

    @ManyToOne
    @JoinColumn(name = "despacho_detalle_id")
    private DespachoDetalle despachoDetalle;

    @ManyToOne
    @JoinColumn(name = "movimiento_original_id")
    private MovimientoInventario movimientoOriginal;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;
}
