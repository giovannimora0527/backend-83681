package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/** Solicitud para despachar las cantidades pendientes de una fórmula vigente. */
@Schema(example = """
        {
          "formulaId": 24,
          "observacion": "Preparación de entrega"
        }
        """)
@Data
public class DespachoRq {

    private Long formulaId;
    private String observacion;
}
