package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos para agregar una anotación a una historia clínica existente.
 */
@Data
public class AnotacionHistoriaRq {

    /**
     * Historia clínica a la que se agrega la anotación.
     */
    private Long historiaId;

    /**
     * Veterinario que registra la anotación.
     */
    private Long medicoId;

    /**
     * Fecha y hora de la anotación.
     */
    private LocalDateTime fechaAnotacion;

    /**
     * Descripción de lo observado o realizado.
     */
    private String descripcion;

    /**
     * Observación adicional.
     */
    private String observacion;

    /**
     * Diagnóstico o evolución del paciente.
     */
    private String diagnosticoEvolucion;
}
