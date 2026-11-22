package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Medicamento o producto médico del inventario. Las existencias reales se manejan por lote
 * (medicamento_lote); stock_actual es el consolidado que se recalcula en cada movimiento.
 */
@Entity
@Table(name = "medicamento")
@Data
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "codigo", nullable = false, length = 8)
    private String codigo;

    @Column(name = "tipo", nullable = false, length = 12)
    private String tipo;

    @Column(name = "nombre", nullable = false, length = 100, unique = true)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "presentacion", length = 100)
    private String presentacion;

    @Column(name = "requiere_invima", nullable = false)
    private Boolean requiereInvima;

    @Column(name = "registro_invima", length = 50)
    private String registroInvima;

    @Column(name = "precio_compra", nullable = false)
    private BigDecimal precioCompra;

    @Column(name = "precio_venta", nullable = false)
    private BigDecimal precioVenta;

    @Column(name = "iva_porcentaje", nullable = false)
    private BigDecimal ivaPorcentaje;

    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(name = "fecha_proximo_pedido")
    private LocalDate fechaProximoPedido;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    @Column(name = "fecha_vence")
    private LocalDate fechaVence;

    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacionRegistro;

    @Column(name = "fecha_modificacion_registro")
    private LocalDateTime fechaModificacionRegistro;
}
