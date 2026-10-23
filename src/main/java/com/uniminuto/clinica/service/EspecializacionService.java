package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.models.EspecializacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

public interface EspecializacionService {

    List<Especializacion> listarEspecializaciones();

    MiRespuestaRS crearEspecializacion(EspecializacionRq rq);

    MiRespuestaRS actualizarEspecializacion(EspecializacionRq rq);

    MiRespuestaRS eliminarEspecializacion(Long id);
}
