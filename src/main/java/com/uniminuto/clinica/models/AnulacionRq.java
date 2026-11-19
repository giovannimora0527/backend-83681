package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

/** Motivo que se conservará en el historial al anular una factura. */
@Schema(example = """
        {
          "motivo": "Corrección del documento"
        }
        """)
public record AnulacionRq(String motivo) {
}
