package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos que recibe el backend para agregar una anotación a una historia clínica.
 */
@Data
public class AnotacionRq {
    /** Veterinario que escribe la anotación. */
    private Long medicoId;
    /** Fecha y hora de la anotación. */
    private LocalDateTime fechaHora;
    /** Descripción de lo realizado o evidenciado. */
    private String descripcion;
    /** Observación adicional. */
    private String observacion;
    /** Diagnóstico o evolución. */
    private String diagnosticoEvolucion;
}
