package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.EspecializacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/** Define las operaciones del catálogo de especialidades. */
public interface EspecializacionService {

    /** Consulta las especialidades veterinarias. */
    List<Especializacion> listarEspecializaciones();

    /** Busca las especialidades veterinarias. */
    List<Especializacion> buscarEspecializaciones(String termino);

    /** Registra la especialidad veterinaria. */
    MiRespuestaRS crearEspecializacion(EspecializacionRq especializacion)
            throws BadRequestException;

    /** Actualiza la especialidad veterinaria. */
    MiRespuestaRS actualizarEspecializacion(EspecializacionRq especializacion)
            throws BadRequestException;
}
