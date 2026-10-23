package com.uniminuto.clinica.service;

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
     * Consulta una mascota por su identificador.
     *
     * @param mascotaId identificador de la mascota.
     * @return mascota encontrada.
     * @throws com.uniminuto.clinica.exception.NotFoundException si la mascota no existe.
     */
    Mascota consultarMascota(Long mascotaId);

    /**
     * Lista las mascotas de un cliente, ordenadas por nombre.
     *
     * @param clienteId identificador del cliente.
     * @return mascotas del cliente.
     * @throws com.uniminuto.clinica.exception.NotFoundException si el cliente no existe.
     */
    List<Mascota> listarMascotasPorCliente(Long clienteId);

    MiRespuestaRS eliminarMascota(Long mascotaId);
}
