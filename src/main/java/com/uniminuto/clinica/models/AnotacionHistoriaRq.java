package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/** Datos clínicos de una nota y sus referencias a la atención y al paciente. */
@Schema(example = """
        {
          "citaId": 9,
          "historiaId": 1,
          "medicoId": 14,
          "mascotaId": 22,
          "descripcion": "Control de evolución",
          "diagnostico": "En seguimiento",
          "observaciones": "Continuar control"
        }
        """)
@Data
public class AnotacionHistoriaRq {
    private Long id;
    private Long citaId;
    private Long historiaId;
    private Long medicoId;
    private String descripcion;
    private String diagnostico;
    private String observaciones;
    private Long mascotaId;
}
