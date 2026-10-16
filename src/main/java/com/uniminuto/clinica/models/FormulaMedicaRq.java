package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FormulaMedicaRq {

    private Long id;
    private Long citaId;
    private Long medicamentoId;
    private String dosis;
    private String frecuencia;
    private String duracionTratamiento;
    private String indicaciones;
    private LocalDateTime fechaEmision;
}