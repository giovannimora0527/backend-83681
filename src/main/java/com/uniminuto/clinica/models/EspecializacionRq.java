package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class EspecializacionRq {
    private Long id;
    private String nombre;
    private String descripcion;
    private String codigoEspecializacion;
}
