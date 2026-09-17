// service/HistoriaMedicaService.java
package com.uniminuto.clinica.service;

import java.util.List;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

public interface HistoriaMedicaService {
    List<HistoriaMedica> listar();
    MiRespuestaRS guardarHistoria(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException;
    MiRespuestaRS actualizarHistoria(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException;
}