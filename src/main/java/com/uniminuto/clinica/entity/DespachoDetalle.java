package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Línea de un despacho que registra el lote y la cantidad efectivamente entregada. */
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

    /** Nombre del medicamento guardado como dato de consulta. */
    @Transient
    private String medicamentoNombre;

    /** Número de lote usado en la dispensación. */
    @Transient
    private String numeroLote;

    /** Fecha de vencimiento del lote usado en la dispensación. */
    @Transient
    private java.time.LocalDate fechaVencimientoLote;

    /** Estado actual del despacho al consultar su trazabilidad. */
    @Transient
    private String estadoDespacho;

    /** Identificador del movimiento de inventario generado para este detalle. */
    @Transient
    private Long movimientoInventarioId;

    /** Identificador del movimiento de reversión, si el despacho fue cancelado. */
    @Transient
    private Long movimientoReversionId;

    /** Fecha y hora de la salida original de inventario. */
    @Transient
    private java.time.LocalDateTime movimientoFecha;

    /** Usuario responsable de la salida original. */
    @Transient
    private Long movimientoUsuarioId;

    /** Nombre del usuario responsable de la salida original. */
    @Transient
    private String movimientoUsuarioNombre;

    /** Fecha y hora de la reversión asociada, cuando existe. */
    @Transient
    private java.time.LocalDateTime movimientoReversionFecha;

    /** Usuario responsable de la reversión asociada, cuando existe. */
    @Transient
    private Long movimientoReversionUsuarioId;

    /** Nombre del usuario responsable de la reversión. */
    @Transient
    private String movimientoReversionUsuarioNombre;
}
