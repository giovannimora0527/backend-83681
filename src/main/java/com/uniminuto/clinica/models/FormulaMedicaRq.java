package com.uniminuto.clinica.models;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Datos que recibe el backend para crear o editar una fórmula médica.
 */
@Data
public class FormulaMedicaRq {
    /** Cliente dueño de la mascota. */
    private Long clienteId;
    /** Mascota a la que se formula. */
    private Long mascotaId;
    /** Historia clínica (consulta) asociada. */
    private Long historiaId;
    /** Veterinario que emite la fórmula. */
    private Long medicoId;
    /** Fecha de emisión (no puede ser anterior a la consulta). */
    private LocalDateTime fechaEmision;
    /** Medicamento formulado. */
    private Long medicamentoId;
    /** Dosis por toma (mayor que cero). */
    private BigDecimal dosis;
    /** Unidad de la dosis. */
    private String unidadDosis;
    /** Cada cuántas horas se administra (1 a 72). */
    private Integer frecuenciaHoras;
    /** Duración del tratamiento en días (1 a 365). */
    private Integer duracionDias;
    /** Indicaciones para el cliente. */
    private String indicaciones;
}
