package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos del servicio que se va a cobrar, con precio, IVA, vigencia y especialidad cuando
 * corresponda.
 */
@Schema(example = """
        {
          "especializacionId": 16,
          "nombreServicio": "Consulta de seguimiento",
          "valorBase": 65000,
          "ivaPorcentaje": 0,
          "activo": true,
          "fechaVigencia": "2026-11-01"
        }
        """)
public record TarifaRq(Long especializacionId, String nombreServicio, BigDecimal valorBase,
                       BigDecimal ivaPorcentaje, Boolean activo, LocalDate fechaVigencia) {
}
