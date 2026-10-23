package com.uniminuto.clinica.models;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos para crear o editar una fórmula médica.
 */
@Data
public class FormulaMedicaRq {

    /**
     * Cliente dueño de la mascota.
     */
    private Long clienteId;

    /**
     * Mascota a la que se formula. Debe pertenecer al cliente.
     */
    private Long mascotaId;

    /**
     * Historia clínica (consulta) asociada. Debe ser de la misma mascota.
     */
    private Long historiaId;

    /**
     * Veterinario que emite la fórmula.
     */
    private Long medicoId;

    /**
     * Fecha de emisión (formato yyyy-MM-dd).
     */
    private LocalDate fechaEmision;

    /**
     * Nombre del medicamento.
     */
    private String medicamento;

    /**
     * Cantidad de la dosis.
     */
    private BigDecimal dosis;

    /**
     * Unidad de la dosis (mg, ml, tableta, gota, g, UI).
     */
    private String unidadDosis;

    /**
     * Frecuencia de administración en horas (cada N horas).
     */
    private Integer frecuenciaHoras;

    /**
     * Duración del tratamiento en días.
     */
    private Integer duracionDias;

    /**
     * Indicaciones para el cliente.
     */
    private String indicaciones;
}
