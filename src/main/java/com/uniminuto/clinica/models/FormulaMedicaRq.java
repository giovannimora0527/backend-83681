package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDate;

/**
 * Datos de entrada para registrar o actualizar una fórmula médica.
 * idMedicamento es obligatorio; idHistoria es opcional y debe corresponder a la misma consulta (cita).
 */
@Data
public class FormulaMedicaRq {

    private Long id;

    private Integer mascotaId;

    private Long citaId;

    private Long medicoId;

    private Long idMedicamento;

    private Long idHistoria;

    private String dosis;

    private String frecuencia;

    private Integer duracionDias;

    private String indicaciones;

    private LocalDate fechaEmision;
}
