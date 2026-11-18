package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.EstadoCita;
import com.uniminuto.clinica.models.CitaMedicaRq;
import com.uniminuto.clinica.models.CitaMedicaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaService {
    List<CitaMedicaRs> listarCitasRango(Long medicoId, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    List<CitaMedicaRs> listarCitasPorMascota(Integer mascotaId);
    MiRespuestaRS crearCita(CitaMedicaRq rq);
    MiRespuestaRS actualizarCita(CitaMedicaRq rq);
    MiRespuestaRS cambiarEstado(Long citaId, EstadoCita estado);
}
