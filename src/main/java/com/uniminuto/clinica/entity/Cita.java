package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "cita")
/**
 * Representa la entidad cita en la base de datos.
 * Contiene información sobre la cita, incluyendo referencias al cliente, médico y mascota.
 */
public class Cita {

    /**
     * Identificador único de la cita (PK auto-generado).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Referencia al cliente asociado a la cita.
     */
    @Column(name = "cliente_id")
    private Long clienteId;

    /**
     * Referencia al médico asociado a la cita.
     */
    @Column(name = "medico_id")
    private Long medicoId;

    /**
     * Referencia a la mascota asociada a la cita.
     */
    @Column(name = "mascota_id")
    private Long mascotaId;

    /**
     * Fecha y hora de la cita.
     */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /**
     * Motivo de la cita.
     */
    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    /**
     * Estado de la cita.
     */
    @Column(name = "estado", length = 20)
    private String estado;
}
