package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** Monto, método y observación del pago asociado a una factura. */
@Schema(example = """
        {
          "monto": 10000,
          "metodo": "EFECTIVO",
          "observacion": "Primer abono"
        }
        """)
public record PagoRq(BigDecimal monto, String metodo, String observacion) {
}
