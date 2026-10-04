package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class MedicamentoRq {
    private Long id;
    private String nombre;
    private String presentacion;
    private String descripcion;
}
