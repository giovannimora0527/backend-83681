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

    MiRespuestaRS guardarMascota(MascotaRq mascotaRq) throws BadRequestException;

    MiRespuestaRS actualizarMascota(MascotaRq mascotaRq) throws BadRequestException;

    /**
     * Lista las mascotas de un cliente.
     *
     * @param clienteId id del cliente.
     * @return mascotas del cliente ordenadas por nombre.
     * @throws BadRequestException si el cliente no existe (404).
     */
    List<Mascota> listarMascotasPorCliente(Long clienteId) throws BadRequestException;

    /**
     * Consulta una mascota por su id.
     *
     * @param mascotaId id de la mascota.
     * @return mascota encontrada.
     * @throws BadRequestException si la mascota no existe (404).
     */
    Mascota consultarMascota(Long mascotaId) throws BadRequestException;
}
