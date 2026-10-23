package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class EspecializacionRequest {
    private String nombre;
    private String descripcion;
    private String codigoEspecializacion;
}
