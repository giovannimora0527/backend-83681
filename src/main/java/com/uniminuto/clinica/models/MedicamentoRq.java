package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDate;

/**
 * Datos de entrada para registrar o actualizar un medicamento.
 */
@Data
public class MedicamentoRq {

    private Long id;

    private String nombre;

    private String descripcion;

    private String presentacion;

    private LocalDate fechaCompra;

    private LocalDate fechaVence;
}
