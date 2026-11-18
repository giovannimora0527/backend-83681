package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos para crear o editar una cita. El motivo de cancelación se solicita cuando el nuevo estado
 * es cancelada.
 */
@Schema(example = """
        {
          "medicoId": 14,
          "mascotaId": 22,
          "fechaHora": "2027-01-18T09:00:00",
          "motivo": "Control preventivo",
          "duracionMinutos": 30
        }
        """)
@Data
public class CitaRq {

    private Long id;
    private Long mascotaId;
    private Long medicoId;
    private LocalDateTime fechaHora;
    private String motivo;
    private String estado;
    private Integer duracionMinutos;
    private String motivoCancelacion;
}
