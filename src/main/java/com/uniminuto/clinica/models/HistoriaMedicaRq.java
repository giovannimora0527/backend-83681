package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/** Datos necesarios para registrar o corregir el diagnóstico de una cita. */
@Schema(example = """
        {
          "citaId": 9,
          "diagnostico": "Valoración preventiva"
        }
        """)
@Data
public class HistoriaMedicaRq {

    private Long id;

    private Long citaId;

    private String diagnostico;

}
