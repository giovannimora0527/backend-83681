package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;
import java.util.Optional;

public interface FormulaMedicaService {

    List<FormulaMedica> findAllFormulas();

    Optional<FormulaMedica> buscarPorId(Long id);

    MiRespuestaRS guardar(FormulaMedicaRq solicitud);

    MiRespuestaRS actualizar(FormulaMedicaRq solicitud);
}