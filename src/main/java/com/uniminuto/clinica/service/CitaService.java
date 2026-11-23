package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.CerrarConsultaRq;

import java.time.LocalDateTime;
import java.util.List;
import com.uniminuto.clinica.models.DisponibilidadRs;
import java.time.LocalDate;

public interface CitaService {

    List<Cita> listarCitas() throws BadRequestException;

    List<Cita> listarCitasRango(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws BadRequestException;

    List<Cita> listarCitasPorMascota(Integer mascotaId) throws BadRequestException;

    MiRespuestaRS crearCita(CitaRq citaRq) throws BadRequestException;

    MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException;

    MiRespuestaRS cerrarConsulta(Long citaId, CerrarConsultaRq request) throws BadRequestException;

    DisponibilidadRs consultarDisponibilidad(Long medicoId, LocalDate fecha, Integer duracionMinutos);
}
