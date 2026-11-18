package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CitaMedicaRq {
    private Long id;
    private Integer mascotaId;
    private Long medicoId;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private String motivoConsulta;
}
