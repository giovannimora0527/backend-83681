package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import java.util.List;

public interface AnotacionHistoriaService {

    List<AnotacionHistoria> listarAnotaciones() throws BadRequestException;

    List<AnotacionHistoria> listarAnotacionesPorHistoria(Long historiaId) throws BadRequestException;

    MiRespuestaRS crearAnotacionHistoria(AnotacionHistoriaRq request) throws BadRequestException;
}