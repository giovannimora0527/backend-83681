package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MascotaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

public interface MascotaService {
    List<Mascota> getListarMascotas();

    Mascota buscarMascotaPorNombre(String nombre) throws BadRequestException;

    List<Mascota> getListarMascotasOrdenadas(boolean ascendente);

    /**
     * Lista las mascotas de un cliente (404 si el cliente no existe).
     *
     * @param clienteId identificador del cliente.
     * @return mascotas del cliente ordenadas por nombre.
     */
    List<Mascota> listarMascotasPorCliente(Long clienteId);

    /**
     * Consulta una mascota por su identificador (404 si no existe).
     *
     * @param mascotaId identificador de la mascota.
     * @return la mascota encontrada.
     */
    Mascota consultarMascota(Long mascotaId);

    MiRespuestaRS guardarMascota(MascotaRq mascotaRq) throws BadRequestException;

    MiRespuestaRS actualizarMascota(MascotaRq mascotaRq) throws BadRequestException;
}
