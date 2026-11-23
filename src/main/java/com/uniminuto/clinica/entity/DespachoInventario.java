package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "despacho_inventario")
@Data
public class DespachoInventario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "formula_id", nullable = false)
    private FormulaMedica formula;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @Column(name = "clave_idempotencia", nullable = false, unique = true, length = 64)
    private String claveIdempotencia;
    @Column(name = "cantidad_solicitada", nullable = false)
    private Integer cantidadSolicitada;
    @Column(name = "cantidad_despachada", nullable = false)
    private Integer cantidadDespachada;
    @Column(nullable = false)
    private LocalDateTime fecha;
}
