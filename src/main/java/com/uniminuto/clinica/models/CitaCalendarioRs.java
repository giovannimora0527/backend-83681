package com.uniminuto.clinica.models;

import java.time.LocalDateTime;

/**
 * Datos de la cita que necesita el calendario, incluidos el paciente, el propietario, el estado y
 * su intervalo.
 */
public record CitaCalendarioRs(Long id, Long medicoId, Long mascotaId, String mascota,
                              String propietario, LocalDateTime inicio, LocalDateTime fin,
                              String motivo, String estado, Long propietarioId, Integer edadMascota,
                              String razaMascota, String especieMascota) {
}
