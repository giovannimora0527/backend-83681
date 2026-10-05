package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;

import java.util.List;

public interface RazaService {
    List<Raza> listarRazas();
    Raza guardarRaza(Raza raza) throws BadRequestException;
    Raza actualizarRaza(Raza raza) throws BadRequestException;
}
