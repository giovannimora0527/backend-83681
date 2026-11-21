package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
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

    @Column(name = "fecha_compra", nullable = false)
    private LocalDate fechaCompra;

    @Column(name = "fecha_vence", nullable = false)
    private LocalDate fechaVence;

    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacionRegistro;

    @Column(name = "fecha_modificacion_registro")
    private LocalDateTime fechaModificacionRegistro;
    @Column(name = "codigo", nullable = false, length = 8, unique = true)
    private String codigo;
    @Column(name = "tipo_producto", nullable = false, length = 20)
    private String tipoProducto = "MEDICAMENTO";
    @Column(name = "registro_invima", length = 50)
    private String registroInvima;
    @Column(name = "requiere_invima", nullable = false)
    private Boolean requiereInvima = true;
    @Column(name = "precio_compra", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioCompra = BigDecimal.ZERO;
    @Column(name = "precio_venta", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVenta = BigDecimal.ZERO;
    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual = 0;
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 0;
    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;
    @Column(name = "fecha_proximo_pedido")
    private LocalDate fechaProximoPedido;
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
    @PrePersist
    public void antesDeCrear() {
        if (fechaCreacionRegistro == null) fechaCreacionRegistro = LocalDateTime.now();
        if (tipoProducto == null) tipoProducto = "MEDICAMENTO";
        if (precioCompra == null) precioCompra = BigDecimal.ZERO;
        if (precioVenta == null) precioVenta = BigDecimal.ZERO;
        if (stockActual == null) stockActual = 0;
        if (stockMinimo == null) stockMinimo = 0;
        if (requiereInvima == null) requiereInvima = "MEDICAMENTO".equalsIgnoreCase(tipoProducto);
        if (activo == null) activo = true;
    }
    @PreUpdate
    public void antesDeEditar() { fechaModificacionRegistro = LocalDateTime.now(); }
}

