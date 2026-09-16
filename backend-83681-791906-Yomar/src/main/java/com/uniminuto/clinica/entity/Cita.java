package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Data
@Table(name = "cita")
public class Cita {

    /**
     * Identificador único de la cita.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Cliente responsable de la cita.
     */
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    /**
     * Mascota que será atendida en la cita.
     */
    @ManyToOne
    @JoinColumn(name = "mascota_id")
    private Mascota mascota;

    /**
     * Médico asignado para atender la cita.
     */
    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;

    /**
     * Fecha y hora programada para la cita.
     */
    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    /**
     * Estado actual de la cita.
     */
    @Column(name = "estado")
    private String estado;

    /**
     * Motivo de la consulta.
     */
    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;
}