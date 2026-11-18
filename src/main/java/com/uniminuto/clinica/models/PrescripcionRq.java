package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Indicaciones generales y lista de medicamentos que el veterinario prescribe en una cita. */
@Schema(example = """
        {
          "indicaciones": "Seguimiento en consulta",
          "medicamentos": [
            {
              "medicamentoId": 7,
              "cantidad": 2,
              "dosis": "Según valoración veterinaria",
              "frecuencia": "Según pauta veterinaria",
              "duracion": "Hasta el control",
              "via": "Oral",
              "indicaciones": "Revisar tolerancia"
            }
          ]
        }
        """)
public record PrescripcionRq(String indicaciones, List<MedicamentoPrescrito> medicamentos) {
    /** Datos de cada medicamento, incluida la vía de administración y la cantidad que se entregará. */
    public record MedicamentoPrescrito(Long medicamentoId, Integer cantidad, String dosis, String frecuencia,
                                       String duracion, String via, String indicaciones) {
    }
}
