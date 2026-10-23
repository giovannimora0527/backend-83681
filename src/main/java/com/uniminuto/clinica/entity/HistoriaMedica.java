package com.uniminuto.clinica.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Historia clínica: registro de una consulta realizada a una mascota. Nace de una cita atendida
 * y puede tener múltiples anotaciones y fórmulas médicas.
 */
@Entity
@Data
@Table(name = "historias_medicas")
public class HistoriaMedica {

    /** Identificador único de la historia clínica. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Mascota a la que pertenece la historia. */
    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    /** Cita atendida que originó la historia (una cita genera máximo una historia). */
    @ManyToOne
    @JoinColumn(name = "cita_id")
    private Cita cita;

    /** Veterinario que realizó la consulta. */
    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;

    /** Fecha y hora de la consulta. */
    @Column(name = "fecha_consulta")
    private LocalDateTime fechaConsulta;

    /** Fecha en que el sistema registró la historia. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    /** Motivo de la consulta. */
    @Column(name = "motivo_consulta", length = 200)
    private String motivoConsulta;

    /** Diagnóstico del veterinario. */
    @Column(name = "diagnostico", length = 500)
    private String diagnostico;

    /** Observaciones generales. */
    @Column(name = "observaciones_generales", length = 500)
    private String observaciones;

    /** Estado de la historia (ABIERTA o CERRADA). */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;
}
