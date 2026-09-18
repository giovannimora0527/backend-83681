package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad que representa una anotación registrada dentro de una historia médica.
 * Está mapeada directamente a la tabla "anotacion_historia" de la base de datos.
 */
@Entity
@Table(name = "anotacion_historia")
@Data
public class AnotacionHistoria {

    /**
     * Identificador único de la anotación (llave primaria, autoincremental).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Historia médica a la que pertenece esta anotación.
     */
    @ManyToOne
    @JoinColumn(name = "historia_id")
    private HistoriaMedica historia;

    /**
     * Médico que registró la anotación.
     */
    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;

    /**
     * Fecha y hora en que se registró la anotación.
     */
    @Column(name = "fecha")
    private LocalDateTime fecha;

    /**
     * Descripción del hallazgo o novedad registrada por el médico.
     */
    @Column(name = "descripcion")
    private String descripcion;
}