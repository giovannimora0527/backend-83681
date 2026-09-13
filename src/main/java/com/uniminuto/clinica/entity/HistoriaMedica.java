package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla 'historia_medica' en la base de datos.
 */
@Entity
@Data
@Table(name = "historia_medica")
public class HistoriaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "paciente_id")
    private Integer pacienteId;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
