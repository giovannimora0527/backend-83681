package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * DTO para la recepción de datos de creación y actualización de anotaciones.
 */
@Data
public class AnotacionHistoriaRq {
    private Long id;
    private Long historiaId;
    private Integer medicoId;
    private String descripcion;
}
