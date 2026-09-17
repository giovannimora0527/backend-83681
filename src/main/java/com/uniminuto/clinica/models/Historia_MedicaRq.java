package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Modelo de entrada para crear o actualizar una historia médica.
 */
@Data
public class Historia_MedicaRq {

    /** Identificador de la historia (solo para actualización). */
    private Long id;

    /** Identificador del cliente asociado a la historia. */
    private Integer clienteId;

    /** Fecha de creación de la historia. Si llega nula se asigna automáticamente. */
    private LocalDateTime fechaCreacion;
}
