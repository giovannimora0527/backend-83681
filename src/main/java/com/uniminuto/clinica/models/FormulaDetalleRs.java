package com.uniminuto.clinica.models;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Fórmula médica con TODOS sus datos, usada en el detalle y en la edición.
 */
@Data
public class FormulaDetalleRs {
    /** Identificador de la fórmula. */
    private Long id;
    /** Fecha en que el sistema registró la fórmula. */
    private LocalDateTime fechaCreacion;
    /** Fecha de emisión. */
    private LocalDateTime fechaEmision;
    /** Identificador del cliente. */
    private Long clienteId;
    /** Nombre completo del cliente. */
    private String clienteNombre;
    /** Identificador de la mascota. */
    private Long mascotaId;
    /** Nombre de la mascota. */
    private String mascotaNombre;
    /** Identificador de la historia clínica asociada. */
    private Long historiaId;
    /** Fecha y hora de la consulta asociada. */
    private LocalDateTime fechaConsulta;
    /** Identificador del veterinario. */
    private Long medicoId;
    /** Nombre completo del veterinario. */
    private String medicoNombre;
    /** Identificador del medicamento. */
    private Long medicamentoId;
    /** Nombre del medicamento. */
    private String medicamentoNombre;
    /** Dosis por toma. */
    private BigDecimal dosis;
    /** Unidad de la dosis. */
    private String unidadDosis;
    /** Cada cuántas horas se administra. */
    private Integer frecuenciaHoras;
    /** Duración del tratamiento en días. */
    private Integer duracionDias;
    /** Indicaciones. */
    private String indicaciones;
    /** Estado de la fórmula. */
    private String estado;
}
