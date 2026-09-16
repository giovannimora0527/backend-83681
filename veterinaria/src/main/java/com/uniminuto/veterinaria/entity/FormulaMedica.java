package com.uniminuto.veterinaria.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

/**
 * Entidad que representa una Fórmula Médica en la Clínica Veterinaria.
 */
@Entity
public class FormulaMedica {

    /** Identificador único de la fórmula médica */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Descripción de la fórmula y medicamentos recetados */
    private String descripcion;

    /** Nombre del paciente o mascota asignada */
    private String paciente;

    /** Fecha y hora en la que se creó la fórmula médica */
    private LocalDateTime fechaCreacion;

    /** Constructor por defecto requerido por JPA */
    public FormulaMedica() {
    }

    /**
     * Constructor con parámetros para instanciar fórmulas médicas.
     * @param descripcion Detalle de los medicamentos
     * @param paciente Nombre de la mascota
     * @param fechaCreacion Fecha de generación
     */
    public FormulaMedica(String descripcion, String paciente, LocalDateTime fechaCreacion) {
        this.descripcion = descripcion;
        this.paciente = paciente;
        this.fechaCreacion = fechaCreacion;
    }

    // Getters y Setters
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

    public String getPaciente() {
        return paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}