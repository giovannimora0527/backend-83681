package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** Importe del descuento y motivo de la autorización administrativa. */
@Schema(example = """
        {
          "monto": 5000,
          "motivo": "Descuento autorizado"
        }
        """)
public record DescuentoRq(BigDecimal monto, String motivo) {
}
