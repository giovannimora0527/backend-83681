package com.uniminuto.clinica.models;

import lombok.Data;
import java.time.LocalTime;

@Data
public class HorarioMedicoRs {
    private Long id;
    private Long medicoId;
    private Integer diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Boolean activo;
}
