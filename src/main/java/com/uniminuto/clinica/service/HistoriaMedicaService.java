package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.models.HistoriaMedicaFiltroRq;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;

import java.util.List;

/**
 * Servicio de negocio para la gestión de historias clínicas (consultas).
 */
public interface HistoriaMedicaService {

    /**
     * Lista las historias clínicas paginadas, filtradas y ordenadas. Por
     * defecto se ordena por fecha y hora de la consulta descendente.
     *
     * @param filtro filtros, página y ordenamiento.
     * @return página de historias clínicas.
     */
    PaginaRs<HistoriaMedica> listarHistorias(HistoriaMedicaFiltroRq filtro);

    /**
     * Consulta una historia clínica por su identificador.
     *
     * @param historiaId identificador de la historia.
     * @return historia encontrada.
     */
    HistoriaMedica consultarHistoria(Long historiaId);

    /**
     * Crea una historia clínica a partir de una cita atendida.
     *
     * @param rq datos de la historia.
     * @return respuesta con el id de la historia creada.
     */
    MiRespuestaRS crearHistoria(HistoriaMedicaRq rq);

    /**
     * Cierra una historia clínica: a partir de ese momento no admite nuevas anotaciones.
     *
     * @param historiaId identificador de la historia.
     * @return respuesta de la operación.
     */
    MiRespuestaRS cerrarHistoria(Long historiaId);

    /**
     * Lista las historias clínicas de una mascota (combo dependiente del
     * formulario de fórmulas médicas).
     *
     * @param mascotaId identificador de la mascota.
     * @return historias de la mascota, de la más reciente a la más antigua.
     */
    List<HistoriaMedica> listarPorMascota(Long mascotaId);
}
