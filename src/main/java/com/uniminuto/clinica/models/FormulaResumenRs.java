package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Fórmula médica con información RESUMIDA, usada en el listado.
 */
@Data
public class FormulaResumenRs {
    /** Identificador de la fórmula. */
    private Long id;
    /** Fecha en que el sistema registró la fórmula. */
    private LocalDateTime fechaCreacion;
    /** Fecha de emisión. */
    private LocalDateTime fechaEmision;
    /** Nombre de la mascota. */
    private String mascotaNombre;
    /** Nombre completo del cliente. */
    private String clienteNombre;
    /** Nombre completo del veterinario. */
    private String medicoNombre;
    /** Nombre del medicamento. */
    private String medicamentoNombre;
    /** Estado de la fórmula. */
    private String estado;
}
