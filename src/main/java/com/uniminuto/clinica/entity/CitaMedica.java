package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla "cita".
 * Una cita pertenece a una mascota y es atendida por un medico (veterinario).
 * El cliente NO se guarda en la cita: se obtiene a traves de la mascota
 * (cita.mascota.cliente), asi se evita informacion duplicada o incoherente.
 */
@Entity
@Table(name = "cita")
@Data
public class CitaMedica {

    /**
     * Identificador unico de la cita.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cita_id")
    private Long id;

    /**
     * Mascota (paciente) de la cita. Por medio de ella se conoce el cliente.
     */
    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    /**
     * Veterinario asignado a la cita.
     */
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    /**
     * Fecha y hora en que se realizara la cita.
     */
    @Column(name = "fecha_cita", nullable = false)
    private LocalDateTime fechaHora;

    /**
     * Estado de la cita: PENDIENTE, PROGRAMADA, CONFIRMADA, ATENDIDA o CANCELADA.
     */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    /**
     * Motivo de la consulta.
     */
    @Column(name = "motivo", length = 200)
    private String motivo;
}
