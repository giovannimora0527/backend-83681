package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Datos clínicos y servicios del cierre. Se puede enviar una tarifa individual o una lista de
 * tarifas distintas.
 */
@Schema(example = """
        {
          "tarifaIds": [
            1
          ],
          "motivo": "Control preventivo",
          "sintomas": "Sin síntomas informados",
          "diagnostico": "Valoración preventiva",
          "observaciones": "Seguimiento en consulta",
          "recomendaciones": "Programar control"
        }
        """)
public record AtencionRq(Long tarifaId, String motivo, String sintomas, String diagnostico,
                         String observaciones, String recomendaciones, List<Long> tarifaIds) {

    public AtencionRq(Long tarifaId, String motivo, String sintomas, String diagnostico,
                      String observaciones, String recomendaciones) {
        this(tarifaId, motivo, sintomas, diagnostico, observaciones, recomendaciones, null);
    }
}
