package com.uniminuto.clinica.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
 * Entidad que representa una anotacion asociada a una historia medica.
 * Corresponde a la relacion historia_medica (1) - anotacion_historia (N).
 */
@Entity
@Data
@Table(name = "anotacion_historia")
public class AnotacionHistoria {

    /** Identificador unico de la anotacion. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "anotacion_id")
    private Long anotacionId;

    /** Texto de la anotacion registrada sobre la historia medica. */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /** Fecha en la que se creo la anotacion. */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /** Fecha de la ultima modificacion de la anotacion. */
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    /** Historia medica a la que pertenece la anotacion. */
    @ManyToOne
    @JoinColumn(name = "historia_id")
    @JsonIgnoreProperties("hibernateLazyInitializer")
    private HistoriaMedica historiaMedica;
}
