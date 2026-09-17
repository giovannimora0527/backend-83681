package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Objeto de entrada (DTO) usado para crear o actualizar una historia medica.
 */
@Data
public class HistoriaMedicaRq {

    /** Identificador de la historia medica. Solo se usa al actualizar. */
    private Long historiaId;

    /** Diagnostico registrado en la historia medica. */
    private String diagnostico;

    /** Tratamiento indicado en la historia medica. */
    private String tratamiento;

    /** Identificador de la mascota duena de la historia medica. */
    private Long mascotaId;
}
