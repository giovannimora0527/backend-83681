package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Objeto de solicitud (request) usado para crear o actualizar una
 * anotación de historia médica.
 */
@Data
public class AnotacionHistoriaRq {

    private Long id;
    private Long historiaId;
    private Long medicoId;
    private String descripcion;
}