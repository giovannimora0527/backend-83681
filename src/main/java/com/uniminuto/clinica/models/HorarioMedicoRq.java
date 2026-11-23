package com.uniminuto.clinica.models;

import lombok.Data;
import java.time.LocalTime;

@Data
public class HorarioMedicoRq {
    private Long medicoId;
    /** ISO-8601 weekday: 1 is Monday, 7 is Sunday. */
    private Integer diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
}
