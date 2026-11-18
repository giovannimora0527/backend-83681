package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.EstadoCita;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CitaMedicaRs {
    private Long id;
    private Long propietarioId;
    private String propietarioNombre;
    private Integer mascotaId;
    private String mascotaNombre;
    private Long medicoId;
    private String medicoNombre;
    private String motivoConsulta;
    private EstadoCita estado;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
}
