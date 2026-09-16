package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Objeto de solicitud (request) usado para crear o actualizar una
 * historia médica.
 */
@Data
public class HistoriaMedicaRq {

    private Long id;
    private Long pacienteId;
}