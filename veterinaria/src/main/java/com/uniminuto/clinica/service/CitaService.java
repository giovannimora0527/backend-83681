package com.uniminuto.clinica.service;


import com.uniminuto.clinica.entity.CitaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRequest;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaService {

    List<CitaMedica> filtrarCitasPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFinal);

    MiRespuestaRS crearCita(CitaRequest citaRq) throws BadRequestException;


    MiRespuestaRS actualizarCita(CitaRequest citaRq) throws BadRequestException;
}
