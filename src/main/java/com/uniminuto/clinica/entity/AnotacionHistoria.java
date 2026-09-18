package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad que representa una anotación asociada a una historia médica
 * ({@link HistoriaMedica}) de la Clínica Veterinaria.
 */
@Entity
@Table(name = "anotacion_historia")
@Data
public class AnotacionHistoria {

    /**
     * Identificador único de la anotación.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "anotacion_id")
    private Long anotacionId;

    /**
     * Observación u anotación realizada sobre la historia médica.
     */
    @Column(name = "observacion", nullable = false, columnDefinition = "TEXT")
    private String observacion;

    /**
     * Fecha en la que se creó la anotación.
     */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de la última modificación de la anotación.
     */
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    /**
     * Historia médica a la que pertenece la anotación.
     */
    @ManyToOne
    @JoinColumn(name = "historia_id", nullable = false)
    private HistoriaMedica historiaMedica;

    /**
     * Médico que realiza la anotación.
     */
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;
}
