package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cita;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaService {

    /**
     * Retorna las citas en el rango [fechaInicial, fechaFinal] ordenadas
     * desde la más reciente hasta la más antigua.
     */
    List<Cita> filtrarCitas(LocalDateTime fechaInicial, LocalDateTime fechaFinal);
}
