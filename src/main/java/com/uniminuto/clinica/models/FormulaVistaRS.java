package com.uniminuto.clinica.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Vista de una fórmula médica. El listado muestra solo algunos campos;
 * el detalle (endpoint por id) muestra todos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormulaVistaRS {

    private Long id;
    private Long mascotaId;
    private String mascotaNombre;
    private Long citaId;
    private Long medicoId;
    private String medicoNombre;
    private Long idMedicamento;
    private String medicamento;
    private Long idHistoria;
    private String dosis;
    private String frecuencia;
    private Integer duracionDias;
    private String indicaciones;
    private LocalDate fechaEmision;
    private LocalDateTime fechaCreacionRegistro;
    private LocalDateTime fechaActualizacionRegistro;
}
