package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos para crear o editar una cita. El motivo de cancelación se solicita cuando el nuevo estado
 * es cancelada.
 */
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
