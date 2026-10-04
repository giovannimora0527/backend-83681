package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.EspecializacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

public interface EspecializacionService {

    List<Especializacion> listar();

    MiRespuestaRS guardar(EspecializacionRq rq) throws BadRequestException;

    MiRespuestaRS actualizar(EspecializacionRq rq) throws BadRequestException;
}
