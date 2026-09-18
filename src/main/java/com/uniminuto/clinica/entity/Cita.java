package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad que representa una cita agendada en la clínica veterinaria.
 * Está mapeada directamente a la tabla "cita" de la base de datos.
 */
@Entity
@Table(name = "cita")
@Data
public class Cita {

    /**
     * Identificador único de la cita (llave primaria, autoincremental).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Cliente dueño de la mascota que asiste a la cita.
     */
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    /**
     * Mascota que asiste a la cita.
     */
    @ManyToOne
    @JoinColumn(name = "mascota_id")
    private Mascota mascota;

    /**
     * Médico que atiende la cita.
     */
    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;

    /**
     * Fecha y hora en la que está programada la cita.
     */
    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    /**
     * Estado actual de la cita (ejemplo: "programada", "cancelada", "atendida").
     */
    @Column(name = "estado")
    private String estado;

    /**
     * Motivo de la consulta. Puede ser nulo.
     */
    @Column(name = "motivo")
    private String motivo;
}