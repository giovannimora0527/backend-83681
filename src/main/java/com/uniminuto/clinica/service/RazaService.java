package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.RazaRq;

import java.util.List;

public interface RazaService {

    List<Raza> listar();

    MiRespuestaRS guardar(RazaRq razaRq) throws BadRequestException;

    MiRespuestaRS actualizar(RazaRq razaRq) throws BadRequestException;
}
