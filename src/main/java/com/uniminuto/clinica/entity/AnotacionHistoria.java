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

    @Column(name = "medico_id")
    private Integer medicoId;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @Column(name = "descripcion")
    private String descripcion;
}
