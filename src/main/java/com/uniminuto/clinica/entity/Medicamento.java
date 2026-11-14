package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Representa un medicamento o producto médico administrado por la clínica. */
@Entity
@Table(name = "medicamento")
@Data
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100, unique = true)
    private String nombre;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipoProducto;

    @Column(name = "requiere_invima", nullable = false)
    private Boolean requiereInvima;

    @Column(name = "codigo", length = 8, unique = true)
    private String codigo;

    @Column(name = "registro_invima", length = 50)
    private String registroInvima;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "presentacion", length = 100)
    private String presentacion;

    @Transient
    private LocalDate fechaCompra;

    @Transient
    private LocalDate fechaVence;

    @Column(name = "precio_compra", precision = 12, scale = 2, nullable = false)
    private BigDecimal precioCompra;

    @Column(name = "precio_venta", precision = 12, scale = 2, nullable = false)
    private BigDecimal precioVenta;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    @Column(name = "stock_actual", nullable = false)
    private Integer cantidadActual;

    @Column(name = "fecha_proximo_pedido")
    private LocalDate fechaProximoPedido;

    @ManyToOne
    @JoinColumn(name = "usuario_creacion")
    private Usuario usuarioCreacion;

    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacionRegistro;

    @Column(name = "fecha_modificacion_registro")
    private LocalDateTime fechaModificacionRegistro;

    /** Lotes asociados al medicamento. */
    @OneToMany(mappedBy = "medicamento")
    private List<InventarioLote> lotes;

    /** Movimientos de inventario asociados al medicamento. */
    @OneToMany(mappedBy = "medicamento")
    private List<MovimientoInventario> movimientos;
}
