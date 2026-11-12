package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface EspecializacionService {

    List<Especializacion> listarEspecializaciones();

    List<Especializacion> buscarEspecializaciones(String termino);

    MiRespuestaRS crearEspecializacion(Especializacion especializacion)
            throws BadRequestException;

    MiRespuestaRS actualizarEspecializacion(Especializacion especializacion)
            throws BadRequestException;
}
