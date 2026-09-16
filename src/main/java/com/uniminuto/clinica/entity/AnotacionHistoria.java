package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa una anotación específica asociada a una historia médica.
 */
@Entity
@Table(name = "anotaciones_historia")
public class AnotacionHistoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "historia_medica_id", nullable = false)
    private HistoriaMedica historiaMedica;

    public AnotacionHistoria() {
    }

    public AnotacionHistoria(Long id, String descripcion, LocalDateTime fecha, HistoriaMedica historiaMedica) {
        this.id = id;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.historiaMedica = historiaMedica;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public HistoriaMedica getHistoriaMedica() {
        return historiaMedica;
    }

    public void setHistoriaMedica(HistoriaMedica historiaMedica) {
        this.historiaMedica = historiaMedica;
    }
}