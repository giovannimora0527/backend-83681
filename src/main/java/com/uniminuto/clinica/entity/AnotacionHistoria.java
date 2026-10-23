package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla "anotacion_historia".
 * Es una nota que un veterinario agrega a una historia clinica
 * (por ejemplo, la evolucion del paciente en un control).
 */
@Entity
@Table(name = "anotacion_historia")
@Data
public class AnotacionHistoria {

    /**
     * Identificador unico de la anotacion.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "anotacion_id")
    private Long id;

    /**
     * Historia clinica a la que pertenece la anotacion.
     */
    @ManyToOne
    @JoinColumn(name = "historia_id", nullable = false)
    private HistoriaMedica historia;

    /**
     * Veterinario que escribio la anotacion.
     */
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    /**
     * Fecha y hora en que se registro la anotacion (la asigna el servidor).
     */
    @Column(name = "fecha_anotacion", nullable = false)
    private LocalDateTime fecha;

    /**
     * Descripcion de lo realizado u observado.
     */
    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Observacion adicional (opcional).
     */
    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    /**
     * Diagnostico o evolucion del paciente.
     */
    @Column(name = "diagnostico", columnDefinition = "TEXT")
    private String diagnostico;
}
