package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.FiltroPaginadoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;

/**
 * Servicio de negocio para las anotaciones de las historias clínicas.
 */
public interface AnotacionHistoriaService {

    /**
     * Agrega una anotación a una historia clínica abierta.
     *
     * @param rq datos de la anotación.
     * @return respuesta con el id de la anotación creada.
     */
    MiRespuestaRS crearAnotacion(AnotacionHistoriaRq rq);

    /**
     * Lista las anotaciones de una historia clínica, paginadas y filtradas.
     * Por defecto de la más reciente a la más antigua.
     *
     * @param historiaId identificador de la historia.
     * @param filtro     filtros, página y ordenamiento.
     * @return página de anotaciones.
     */
    PaginaRs<AnotacionHistoria> listarPorHistoria(Long historiaId, FiltroPaginadoRq filtro);

    /**
     * Lista el histórico completo de anotaciones de una mascota (de todas sus
     * historias clínicas), paginado y filtrado.
     *
     * @param mascotaId identificador de la mascota.
     * @param filtro    filtros, página y ordenamiento.
     * @return página de anotaciones.
     */
    PaginaRs<AnotacionHistoria> listarPorMascota(Long mascotaId, FiltroPaginadoRq filtro);

    /**
     * Consulta el detalle de una anotación.
     *
     * @param anotacionId identificador de la anotación.
     * @return anotación encontrada.
     */
    AnotacionHistoria consultarAnotacion(Long anotacionId);
}
