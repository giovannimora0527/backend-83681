package com.uniminuto.veterinaria.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

/**
 * Entidad que representa una Cita en el sistema de la Clínica Veterinaria.
 */
@Entity
public class Cita {

    /** Identificador único de la cita */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de la mascota asignada a la cita */
    private String mascota;

    /** Motivo de la consulta veterinaria */
    private String motivo;

    /** Fecha en la que se programa la cita */
    private LocalDate fecha;

    /** Estado actual de la cita (Ej: PROGRAMADA, ATENDIDA, CANCELADA) */
    private String estado;

    /** Constructor por defecto requerido por JPA */
    public Cita() {
    }

    /**
     * Constructor con parámetros para instanciar citas médicas.
     * @param mascota Nombre del paciente
     * @param motivo Razón de la cita
     * @param fecha Fecha agendada
     * @param estado Estado inicial de la cita
     */
    public Cita(String mascota, String motivo, LocalDate fecha, String estado) {
        this.mascota = mascota;
        this.motivo = motivo;
        this.fecha = fecha;
        this.estado = estado;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMascota() {
        return mascota;
    }

    public void setMascota(String mascota) {
        this.mascota = mascota;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
