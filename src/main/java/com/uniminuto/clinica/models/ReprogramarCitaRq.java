package com.uniminuto.clinica.models;

import java.time.LocalDateTime;

/** Nueva fecha y hora de inicio. La duración y los participantes se toman de la cita existente. */
public record ReprogramarCitaRq(LocalDateTime fechaHora) {
}
