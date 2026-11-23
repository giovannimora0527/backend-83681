package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Datos de entrada para registrar o actualizar una especialización.
 */
@Data
public class EspecializacionRq {

    private Long id;

    private String nombre;

    private String descripcion;

    private String codigoEspecializacion;
}
