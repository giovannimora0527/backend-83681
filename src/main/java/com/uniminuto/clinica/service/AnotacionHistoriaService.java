package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.AnotacionHistoriaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

public interface AnotacionHistoriaService {

    List<AnotacionHistoriaRs> listarAnotaciones();

    List<AnotacionHistoriaRs> buscarPorHistoria(Long historiaId);

    MiRespuestaRS crearAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq);

    MiRespuestaRS actualizarAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq);
}
