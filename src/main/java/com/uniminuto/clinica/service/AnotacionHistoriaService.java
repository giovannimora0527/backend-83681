// service/AnotacionHistoriaService.java
package com.uniminuto.clinica.service;

import java.time.LocalDateTime;
import java.util.List;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

public interface AnotacionHistoriaService {
    List<AnotacionHistoria> listarPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws BadRequestException;
    MiRespuestaRS guardarAnotacion(AnotacionHistoriaRq anotacionRq) throws BadRequestException;
    MiRespuestaRS actualizarAnotacion(AnotacionHistoriaRq anotacionRq) throws BadRequestException;
}