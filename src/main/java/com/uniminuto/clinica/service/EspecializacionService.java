package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.EspecializacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/** Define las operaciones del catálogo de especialidades. */
public interface EspecializacionService {

    List<Especializacion> listarEspecializaciones();

    List<Especializacion> buscarEspecializaciones(String termino);

    MiRespuestaRS crearEspecializacion(EspecializacionRq especializacion)
            throws BadRequestException;

    MiRespuestaRS actualizarEspecializacion(EspecializacionRq especializacion)
            throws BadRequestException;
}
