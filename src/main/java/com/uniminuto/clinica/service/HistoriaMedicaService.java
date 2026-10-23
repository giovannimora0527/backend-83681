package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.AnotacionRq;
import com.uniminuto.clinica.models.AnotacionRs;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.HistoriaMedicaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/**
 * Contrato de la lógica de negocio de las historias clínicas y sus anotaciones.
 */
public interface HistoriaMedicaService {

    /**
     * Lista las historias de la consulta más reciente a la más antigua.
     *
     * @return historias resumidas.
     */
    List<HistoriaMedicaRs> listar();

    /**
     * Consulta una historia clínica.
     *
     * @param id identificador de la historia.
     * @return la historia encontrada.
     */
    HistoriaMedicaRs consultar(Long id);

    /**
     * Lista las historias de una mascota.
     *
     * @param mascotaId identificador de la mascota.
     * @return historias de la mascota.
     */
    List<HistoriaMedicaRs> listarPorMascota(Long mascotaId);

    /**
     * Registra una historia clínica a partir de una cita atendida.
     *
     * @param rq datos de la historia.
     * @return respuesta con el resultado.
     */
    MiRespuestaRS crear(HistoriaMedicaRq rq);

    /**
     * Cierra una historia clínica (deja de admitir anotaciones).
     *
     * @param id identificador de la historia.
     * @return respuesta con el resultado.
     */
    MiRespuestaRS cerrar(Long id);

    /**
     * Lista las anotaciones de una historia, de la más reciente a la más antigua.
     *
     * @param historiaId identificador de la historia.
     * @return anotaciones de la historia.
     */
    List<AnotacionRs> listarAnotaciones(Long historiaId);

    /**
     * Consulta el detalle de una anotación.
     *
     * @param anotacionId identificador de la anotación.
     * @return la anotación encontrada.
     */
    AnotacionRs consultarAnotacion(Long anotacionId);

    /**
     * Agrega una anotación a una historia abierta.
     *
     * @param historiaId identificador de la historia.
     * @param rq datos de la anotación.
     * @return respuesta con el resultado.
     */
    MiRespuestaRS crearAnotacion(Long historiaId, AnotacionRq rq);
}
