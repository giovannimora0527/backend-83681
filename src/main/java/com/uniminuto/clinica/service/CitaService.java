package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.CitaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/**
 * Contrato de la lógica de negocio de las citas médicas.
 */
public interface CitaService {

    /**
     * Lista todas las citas de la más reciente a la más antigua.
     *
     * @return citas con cliente, mascota y veterinario resumidos.
     */
    List<CitaRs> listar();

    /**
     * Consulta una cita.
     *
     * @param id identificador de la cita.
     * @return la cita encontrada.
     */
    CitaRs consultar(Long id);

    /**
     * Lista las citas de una mascota.
     *
     * @param mascotaId identificador de la mascota.
     * @return citas de la mascota, de la más reciente a la más antigua.
     */
    List<CitaRs> listarPorMascota(Long mascotaId);

    /**
     * Registra una nueva cita validando las reglas de negocio.
     *
     * @param rq datos de la cita.
     * @return respuesta con el resultado.
     */
    MiRespuestaRS crear(CitaRq rq);

    /**
     * Edita una cita respetando las restricciones según su estado.
     *
     * @param id identificador de la cita.
     * @param rq datos nuevos.
     * @return respuesta con el resultado.
     */
    MiRespuestaRS editar(Long id, CitaRq rq);

    /**
     * Reactiva una cita cancelada (operación explícita).
     *
     * @param id identificador de la cita.
     * @return respuesta con el resultado.
     */
    MiRespuestaRS reactivar(Long id);
}
