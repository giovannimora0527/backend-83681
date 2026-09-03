package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;

import java.util.List;

public interface MascotaService {
    List<Mascota> getListarMascotas();

    Mascota buscarMascotaPorNombre(String nombre) throws BadRequestException;
}
