package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Datos de entrada para registrar o actualizar una raza.
 */
@Data
public class RazaRq {

    private Integer razaId;

    private String nombre;

    private String especie;
}
