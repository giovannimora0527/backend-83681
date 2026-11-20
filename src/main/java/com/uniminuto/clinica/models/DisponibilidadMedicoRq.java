package com.uniminuto.clinica.models;
import java.time.LocalTime;
public record DisponibilidadMedicoRq(Long medicoId, Integer diaSemana, LocalTime horaInicio, LocalTime horaFin) { }
