package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Historia clínica (registro de consulta) de una mascota.
 * Se genera a partir de una cita atendida (relación 1:1 con la cita) y puede
 * tener varias anotaciones ({@link AnotacionHistoria}) y fórmulas médicas
 * ({@link FormulaMedica}).
 */
@Entity
@Table(name = "historia_medica")
@Data
public class HistoriaMedica {

    /**
     * Identificador de la historia clínica.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "historia_id")
    private Long historiaId;

    /**
     * Cita atendida que originó la historia. Una cita solo puede tener una historia.
     */
    @OneToOne
    @JoinColumn(name = "cita_id", nullable = false, unique = true)
    private Cita cita;

    /**
     * Mascota a la que pertenece la historia.
     */
    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    /**
     * Veterinario que realizó la consulta.
     */
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    /**
     * Fecha y hora en que se realizó la consulta.
     */
    @Column(name = "fecha_consulta", nullable = false)
    private LocalDateTime fechaConsulta;

    /**
     * Motivo de la consulta.
     */
    @Column(name = "motivo_consulta", nullable = false, length = 255)
    private String motivoConsulta;

    /**
     * Diagnóstico emitido por el veterinario.
     */
    @Column(name = "diagnostico", nullable = false, columnDefinition = "TEXT")
    private String diagnostico;

    /**
     * Observaciones generales de la consulta.
     */
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    /**
     * Estado de la historia (abierta o cerrada).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoHistoria estado;

    /**
     * Fecha de creación del registro.
     */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de la última modificación del registro.
     */
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
}
