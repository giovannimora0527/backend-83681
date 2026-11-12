package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MedicamentoRq {

    private Long id;
    private String nombre;
    private String descripcion;
    private String presentacion;
    private LocalDate fechaCompra;
    private LocalDate fechaVence;
}
