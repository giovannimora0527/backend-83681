package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class AnotacionHistoriaRq {

    /**
     * Identificador único de la historia clínica.
     */
    private Long historiaId;

    /**
     * Identificador único del médico.
     */
    private Long medicoId;

    /**
     * Descripción detallada de la anotación.
     */
    private String descripcion;

    /**
     * Observaciones adicionales del médico.
     */
    private String observacion;

    /**
     * Diagnóstico o evolución clínica.
     */
    private String diagnosticoEvolucion;
}