package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** Nueva fecha y hora de inicio. La duración y los participantes se toman de la cita existente. */
@Schema(example = """
        {
          "fechaHora": "2027-01-18T10:00:00"
        }
        """)
public record ReprogramarCitaRq(LocalDateTime fechaHora) {
}
