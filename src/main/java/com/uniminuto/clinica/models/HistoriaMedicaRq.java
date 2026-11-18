package com.uniminuto.clinica.models;

import lombok.Data;

/** Datos necesarios para registrar o corregir el diagnóstico de una cita. */
@Data
public class HistoriaMedicaRq {

    private Long id;

    private Long citaId;

    private String diagnostico;

}
