package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicamento")
@Data
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100, unique = true)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "presentacion", length = 100)
    private String presentacion;

    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    @Column(name = "fecha_vence")
    private LocalDate fechaVence;

    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacionRegistro;

    @Column(name = "fecha_modificacion_registro")
    private LocalDateTime fechaModificacionRegistro;

    @Column(name = "codigo", length = 8, unique = true)
    private String codigo;

    @Column(name = "tipo_producto", nullable = false, length = 20)
    private String tipoProducto = "MEDICAMENTO";

    @Column(name = "registro_invima", length = 80)
    private String registroInvima;

    @Column(name = "precio_compra", nullable = false)
    private java.math.BigDecimal precioCompra = java.math.BigDecimal.ZERO;

    @Column(name = "precio_venta", nullable = false)
    private java.math.BigDecimal precioVenta = java.math.BigDecimal.ZERO;

    @Column(name = "stock_minimo", nullable = false)
    private java.math.BigDecimal stockMinimo = java.math.BigDecimal.ZERO;

    @Column(name = "fecha_proximo_pedido")
    private LocalDate fechaProximoPedido;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
