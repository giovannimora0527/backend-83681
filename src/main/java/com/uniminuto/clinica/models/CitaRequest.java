package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/** Contiene los datos recibidos para registrar una cita. */
@Data
public class CitaRequest {

    private Long id;

    private Long mascotaId;

    private Long medicoId;

    private String motivo;

    private LocalDateTime fechaHora;
}
