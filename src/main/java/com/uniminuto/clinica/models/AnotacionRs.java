package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Anotación de una historia clínica lista para mostrar.
 */
@Data
public class AnotacionRs {
    /** Identificador de la anotación. */
    private Long id;
    /** Historia a la que pertenece. */
    private Long historiaId;
    /** Fecha y hora de la anotación. */
    private LocalDateTime fechaHora;
    /** Identificador del veterinario. */
    private Long medicoId;
    /** Nombre completo del veterinario. */
    private String medicoNombre;
    /** Descripción. */
    private String descripcion;
    /** Observación. */
    private String observacion;
    /** Diagnóstico o evolución. */
    private String diagnosticoEvolucion;
}
