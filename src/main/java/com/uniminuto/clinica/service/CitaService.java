package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.CitaCalendarioRs;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDateTime;
import java.util.List;

/** Define la consulta de agenda y las operaciones de creación, edición y reprogramación de citas. */
public interface CitaService {
    /** Consulta la agenda personal del veterinario por intervalo. */
    List<CitaCalendarioRs> calendario(LocalDateTime inicio, LocalDateTime fin);

    /** Consulta la cita respetando el acceso por veterinario asignado. */
    CitaCalendarioRs detalle(Long id);

    /** Devuelve el médico vinculado a la sesión veterinaria. */
    Long medicoAutenticado();

    /** Mueve la cita a un horario disponible y conserva su duración. */
    CitaCalendarioRs reprogramar(Long id, LocalDateTime fechaHora);

    List<Cita> listarCitas() throws BadRequestException;

    List<Cita> listarCitasRango(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws BadRequestException;

    List<Cita> listarCitasPorMascota(Long mascotaId) throws BadRequestException;

    /** Valida y crea una cita en estado programada. */
    MiRespuestaRS crearCita(CitaRq citaRq) throws BadRequestException;

    /** Actualiza una cita abierta y conserva los datos de cancelación cuando corresponda. */
    MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException;
}
