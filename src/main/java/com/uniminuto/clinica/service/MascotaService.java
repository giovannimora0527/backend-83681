package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MascotaRq;
import com.uniminuto.clinica.models.UsuarioRS;

import java.util.List;

public interface MascotaService {
    List<Mascota> getListarMascotas();

    Mascota buscarMascotaPorNombre(String nombre) throws BadRequestException;

    List<Mascota> getListarMascotasOrdenadas(boolean ascendente);

    UsuarioRS guardarMascota(MascotaRq mascotaRq) throws BadRequestException;

    UsuarioRS actualizarMascota(MascotaRq mascotaRq) throws BadRequestException;
}
