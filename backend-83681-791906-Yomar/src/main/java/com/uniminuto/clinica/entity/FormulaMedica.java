package com.uniminuto.clinica.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "formula_medica")
public class FormulaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;


    @Column(name = "dosis", columnDefinition = "TEXT")
    private String dosis;



    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;



    @Column(name = "fecha_creacion_registro")
    private LocalDateTime fechaCreacionRegistro;



    @Column(name = "fecha_actualizacion_registro")
    private  LocalDateTime fechaActualizacionRegistro;


}
