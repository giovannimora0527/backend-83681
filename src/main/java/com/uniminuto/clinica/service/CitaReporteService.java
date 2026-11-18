package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.EstadoCita;
import com.uniminuto.clinica.entity.Medico;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

public interface CitaReporteService {
    ByteArrayOutputStream exportarExcel(Medico medico, LocalDateTime inicio, LocalDateTime fin);
}
