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
 * Entidad que representa la historia medica de una mascota.
 */
@Entity
@Data
@Table(name = "historia_medica")
public class HistoriaMedica {

    /** Identificador unico de la historia medica. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "historia_id")
    private Long historiaId;

    /** Diagnostico registrado por el medico. */
    @Column(name = "diagnostico", columnDefinition = "TEXT")
    private String diagnostico;

    /** Tratamiento indicado para la mascota. */
    @Column(name = "tratamiento", columnDefinition = "TEXT")
    private String tratamiento;

    /** Fecha en la que se creo la historia medica. */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /** Fecha de la ultima modificacion de la historia medica. */
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    /** Mascota duena de la historia medica. */
    @ManyToOne
    @JoinColumn(name = "mascota_id")
    private Mascota mascota;
}
