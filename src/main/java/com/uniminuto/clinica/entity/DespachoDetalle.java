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
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "despacho_detalle")
@Data
public class DespachoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "despacho_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Despacho despacho;

    @Column(name = "formula_detalle_id")
    private Long formulaDetalleId;

    @ManyToOne
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @ManyToOne
    @JoinColumn(name = "lote_id", nullable = false)
    private InventarioLote lote;

    @Column(name = "cantidad_entregada", nullable = false)
    private Integer cantidadEntregada;

    @Column(name = "cantidad_formulada", nullable = false)
    private Integer cantidadFormulada;
}
