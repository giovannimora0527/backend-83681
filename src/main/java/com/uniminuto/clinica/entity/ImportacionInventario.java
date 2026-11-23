package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "importacion_inventario")
@Data
public class ImportacionInventario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 255)
    private String archivo;
    @Column(nullable = false, length = 64)
    private String sha256;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @Column(name = "total_filas", nullable = false)
    private Integer totalFilas;
    @Column(name = "filas_importadas", nullable = false)
    private Integer filasImportadas;
    @Column(name = "filas_rechazadas", nullable = false)
    private Integer filasRechazadas;
    @Column(nullable = false)
    private LocalDateTime fecha;
}
