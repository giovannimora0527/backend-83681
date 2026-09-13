package com.uniminuto.clinica.service;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.AnotacionHistoriaRs;

import java.time.LocalDate;
import java.util.List;

public interface AnotacionHistoriaService {

    List<AnotacionHistoriaRs> listarAnotaciones(LocalDate fechaInicio, LocalDate fechaFin) throws BadRequestException;

    AnotacionHistoriaRs crearAnotacion(AnotacionHistoriaRq anotacionRq) throws BadRequestException;

    AnotacionHistoriaRs actualizarAnotacion(AnotacionHistoriaRq anotacionRq) throws BadRequestException;
}
