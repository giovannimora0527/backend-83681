package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

/** Referencia del comprobante de devolución de un pago. */
@Schema(example = """
        {
          "referencia": "REV-TR-20261114-0038"
        }
        """)
public record DevolucionRq(String referencia) {
}
