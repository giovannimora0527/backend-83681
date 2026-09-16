package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa la Historia Médica de un paciente/mascota.
 */
@Entity
@Table(name = "historias_medicas")
public class HistoriaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private String observacionesGenerales;

    public HistoriaMedica() {
    }

    public HistoriaMedica(Long id, LocalDateTime fechaCreacion, String observacionesGenerales) {
        this.id = id;
        this.fechaCreacion = fechaCreacion;
        this.observacionesGenerales = observacionesGenerales;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getObservacionesGenerales() {
        return observacionesGenerales;
    }

    public void setObservacionesGenerales(String observacionesGenerales) {
        this.observacionesGenerales = observacionesGenerales;
    }
}