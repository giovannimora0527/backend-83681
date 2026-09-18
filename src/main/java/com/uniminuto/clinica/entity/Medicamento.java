package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa un medicamento del inventario de la clínica veterinaria.
 * Está mapeada directamente a la tabla "medicamento" de la base de datos.
 */
@Entity
@Table(name = "medicamento")
@Data
public class Medicamento {

    /**
     * Identificador único del medicamento (llave primaria, autoincremental).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Nombre comercial del medicamento.
     */
    @Column(name = "nombre")
    private String nombre;

    /**
     * Descripción del medicamento.
     */
    @Column(name = "descripcion")
    private String descripcion;

    /**
     * Presentación del medicamento (ejemplo: "Tabletas 500mg").
     */
    @Column(name = "presentacion")
    private String presentacion;

    /**
     * Fecha en que se compró el lote del medicamento.
     */
    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    /**
     * Fecha de vencimiento del lote del medicamento.
     */
    @Column(name = "fecha_vence")
    private LocalDate fechaVence;

    /**
     * Fecha y hora en que se creó el registro del medicamento.
     */
    @Column(name = "fecha_creacion_registro")
    private LocalDateTime fechaCreacionRegistro;

    /**
     * Fecha y hora de la última actualización del registro.
     */
    @Column(name = "fecha_modificacion_registro")
    private LocalDateTime fechaModificacionRegistro;
}