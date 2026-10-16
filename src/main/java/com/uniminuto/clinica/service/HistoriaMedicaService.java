package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import java.util.List;

public interface HistoriaMedicaService {

    List<HistoriaMedica> listarHistorias() throws BadRequestException;

    MiRespuestaRS crearHistoria(HistoriaMedicaRq request) throws BadRequestException;

}