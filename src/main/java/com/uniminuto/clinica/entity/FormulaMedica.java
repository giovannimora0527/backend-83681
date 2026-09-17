package com.uniminuto.clinica.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * Clase que representa la entidad FormulaMedica en la base de datos.
 * Esta clase se mapea a la tabla "formula_medica" y contiene información sobre las fórmulas médicas asociadas a las citas médicas.
 */
@Entity
@Data
@Table(name = "formula_medica")
public class FormulaMedica {


/**
     * Identificador único de la fórmula médica.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "cita_id")
    private Integer citaId;

    @Column(name = "medicamento_id")
    private Integer medicamentoId;


    @Column(name = "dosis", columnDefinition = "TEXT")
    private String dosis;



    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;



    @Column(name = "fecha_creacion_registro")
    private LocalDateTime fechaCreacionRegistro;



    @Column(name = "fecha_actualizacion_registro")
    private  LocalDateTime fechaActualizacionRegistro;


}
