package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.UsuarioRS;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaService {

    /**
     * Retorna las citas en el rango [fechaInicial, fechaFinal] ordenadas
     * desde la más reciente hasta la más antigua.
     */
    List<Cita> filtrarCitas(LocalDateTime fechaInicial, LocalDateTime fechaFinal);

    UsuarioRS guardarCita(CitaRq citaRq) throws BadRequestException;

    UsuarioRS actualizarCita(CitaRq citaRq) throws BadRequestException;
}
