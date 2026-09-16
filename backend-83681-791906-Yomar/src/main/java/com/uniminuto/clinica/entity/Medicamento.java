package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa un medicamento del inventario de la clínica
 * veterinaria. Se mapea contra la tabla {@code medicamento}.
 */
@Entity
@Data
@Table(name = "medicamento")
public class Medicamento {

    /**
     * Identificador único del medicamento.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Nombre comercial del medicamento. Es único en la base de datos.
     */
    @Column(name = "nombre")
    private String nombre;

    /**
     * Descripción del medicamento.
     */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Presentación del medicamento (por ejemplo: tabletas, jarabe, ampolla).
     */
    @Column(name = "presentacion")
    private String presentacion;

    /**
     * Fecha en que se compró el medicamento.
     */
    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    /**
     * Fecha de vencimiento del medicamento.
     */
    @Column(name = "fecha_vence")
    private LocalDate fechaVence;

    /**
     * Fecha de creación del registro en el sistema.
     */
    @Column(name = "fecha_creacion_registro")
    private LocalDateTime fechaCreacionRegistro;

    /**
     * Fecha de la última modificación del registro.
     */
    @Column(name = "fecha_modificacion_registro")
    private LocalDateTime fechaModificacionRegistro;
}