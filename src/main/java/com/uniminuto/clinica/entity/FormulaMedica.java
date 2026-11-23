package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "formula_medica")
@Data
public class FormulaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    @ManyToOne
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(name = "dosis", nullable = false, columnDefinition = "TEXT")
    private String dosis;

    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;

    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacionRegistro;

    @Column(name = "fecha_actualizacion_registro")
    private LocalDateTime fechaActualizacionRegistro;

    @Column(name = "frecuencia")
    private String frecuencia;

    @Column(name = "duracion")
    private String duracion;

    @Column(name = "cantidad_prescrita", nullable = false)
    private Integer cantidadPrescrita = 1;

    @Column(name = "estado_dispensacion", nullable = false, length = 20)
    private String estadoDispensacion = "PENDIENTE";

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "via_administracion", nullable = false, length = 50)
    private String viaAdministracion = "ORAL";
}
