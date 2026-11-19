package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.AnotacionHistoriaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/** Define las operaciones disponibles para gestionar una anotación del historial clínico. */
public interface AnotacionHistoriaService {

    /** Consulta las anotaciones clínicas registradas. */
    List<AnotacionHistoriaRs> listarAnotaciones();

    /** Consulta las anotaciones de la historia indicada. */
    List<AnotacionHistoriaRs> buscarPorHistoria(Long historiaId);

    /** Registra una nueva anotación en la historia clínica. */
    MiRespuestaRS crearAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq);

    /** Guarda los cambios de una anotación clínica existente. */
    MiRespuestaRS actualizarAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq);
}
