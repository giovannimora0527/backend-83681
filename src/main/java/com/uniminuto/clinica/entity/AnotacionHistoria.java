package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla 'anotacion_historia' en la base de datos.
 */
@Entity
@Data
@Table(name = "anotacion_historia")
public class AnotacionHistoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "historia_id")
    private Long historiaId;

    /** Identificador del médico que realizó la anotación (FK). */
    @Column(name = "medico_id")
    private Integer medicoId;

    /** Fecha y hora en que se creó la anotación. */
    @Column(name = "fecha")
    private LocalDateTime fecha;


    /** Descripción detallada de la anotación clínica realizada por el médico. */
    @Column(name = "descripcion")
    private String descripcion;
}
