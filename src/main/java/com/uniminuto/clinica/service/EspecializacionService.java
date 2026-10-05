package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;

import java.util.List;

public interface EspecializacionService {
    List<Especializacion> listarEspecializaciones();
    Especializacion guardarEspecializacion(Especializacion especializacion) throws BadRequestException;
    Especializacion actualizarEspecializacion(Especializacion especializacion) throws BadRequestException;
}
