package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "importacion_inventario_detalle")
@Data
public class ImportacionInventarioDetalle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "importacion_id", nullable = false)
    private ImportacionInventario importacion;
    @Column(name = "numero_fila", nullable = false)
    private Integer numeroFila;
    @Column(name = "codigo_producto", length = 8)
    private String codigoProducto;
    @Column(name = "codigo_lote", length = 80)
    private String codigoLote;
    @Column(nullable = false, length = 20)
    private String estado;
    @Column(length = 500)
    private String mensaje;
}
