package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** Porcentaje de IVA que se aplica a un medicamento al facturarlo. */
@Schema(example = """
        {
          "ivaPorcentaje": 19
        }
        """)
public record ImpuestoRq(BigDecimal ivaPorcentaje) {
}
