package com.uniminuto.veterinaria.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

/**
 * Entidad que representa la Historia Médica de un paciente en la clínica.
 */
@Entity
public class HistoriaMedica {

    /** Identificador único de la historia médica */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de la mascota registrada */
    private String paciente;

    /** Diagnóstico registrado por el médico veterinario */
    private String diagnostico;

    /** Tratamiento o recomendaciones asignadas */
    private String tratamiento;

    /** Fecha en la que se registra la historia médica */
    private LocalDate fecha;

    /** Constructor por defecto requerido por JPA */
    public HistoriaMedica() {
    }

    /**
     * Constructor con parámetros para instanciar la historia médica.
     * @param paciente Nombre de la mascota
     * @param diagnostico Evaluación médica realizada
     * @param tratamiento Plan de salud asignado
     * @param fecha Fecha del registro
     */
    public HistoriaMedica(String paciente, String diagnostico, String tratamiento, LocalDate fecha) {
        this.paciente = paciente;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.fecha = fecha;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaciente() {
        return paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}