package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "cita") // Nombre de la tabla

public class Cita {

    /** Estructura de la entidad Cita con sus atributos
     *  y distribución a la base de datos
     *  */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "cliente_id")
    private Integer clienteId;

    @Column(name = "mascota_id")
    private Integer mascotaId;

    @Column(name = "medico_id")
    private Integer medicoId;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(name = "estado")
    private String estado;

    @Column(name = "motivo")
    private String motivo;
}

