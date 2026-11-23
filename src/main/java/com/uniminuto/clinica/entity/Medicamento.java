package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

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

    @Deprecated
    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    @Deprecated
    @Column(name = "fecha_vence")
    private LocalDate fechaVence;

    @Column(name = "codigo", length = 8, unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_producto", nullable = false, length = 20)
    private TipoProducto tipoProducto;

    @Column(name = "registro_invima", length = 100)
    private String registroInvima;

    @Column(name = "precio_compra", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioCompra;

    @Column(name = "precio_publico", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioPublico;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacionRegistro;

    @Column(name = "fecha_modificacion_registro")
    private LocalDateTime fechaModificacionRegistro;

    @PrePersist
    void antesDeCrear() {
        if (fechaCreacionRegistro == null) {
            fechaCreacionRegistro = LocalDateTime.now();
        }
        if (tipoProducto == null) {
            tipoProducto = TipoProducto.MEDICAMENTO;
        }
        if (precioCompra == null) {
            precioCompra = BigDecimal.ZERO;
        }
        if (precioPublico == null) {
            precioPublico = BigDecimal.ZERO;
        }
        if (stockMinimo == null) {
            stockMinimo = 0;
        }
        if (activo == null) {
            activo = true;
        }
    }

    @PreUpdate
    void antesDeActualizar() {
        fechaModificacionRegistro = LocalDateTime.now();
    }
}
