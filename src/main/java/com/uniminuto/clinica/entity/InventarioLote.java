package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario_lote", uniqueConstraints = @UniqueConstraint(name = "uk_lote_producto", columnNames = {"medicamento_id", "numero_lote"}))
@Data
public class InventarioLote {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(name = "numero_lote", nullable = false, length = 80)
    private String numeroLote;
    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;
    @Column(name = "cantidad_inicial", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadInicial;
    @Column(name = "cantidad_disponible", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadDisponible;
    @Column(name = "precio_compra_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioCompraUnitario;
    @Column(name = "precio_venta_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVentaUnitario;
    @Column(name = "referencia_proveedor", length = 120)
    private String referenciaProveedor;
    @Column(name = "creado_por")
    private Long creadoPor;
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
