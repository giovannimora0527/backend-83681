package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.EspecializacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/**
 * Operaciones de negocio sobre el catálogo de especializaciones.
 */
public interface EspecializacionService {

    List<Especializacion> listarEspecializaciones();

    Especializacion buscarEspecializacionPorCodigo(String codigo) throws BadRequestException;

    MiRespuestaRS guardarEspecializacion(EspecializacionRq especializacionRq) throws BadRequestException;

    MiRespuestaRS actualizarEspecializacion(EspecializacionRq especializacionRq) throws BadRequestException;
}
