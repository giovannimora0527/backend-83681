package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Datos que envia el frontend para agregar una anotacion a una historia clinica.
 * La fecha no se recibe: la asigna el servidor en el momento de guardar.
 */
@Data
public class AnotacionRq {

    /**
     * Id de la historia clinica a la que se agrega la anotacion.
     */
    private Long historiaId;

    /**
     * Id del veterinario que escribe la anotacion.
     */
    private Long medicoId;

    /**
     * Descripcion de la anotacion (obligatoria).
     */
    private String descripcion;

    /**
     * Observacion adicional (opcional).
     */
    private String observacion;

    /**
     * Diagnostico o evolucion del paciente (obligatorio).
     */
    private String diagnostico;
}
