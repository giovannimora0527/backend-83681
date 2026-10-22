package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.RazaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/**
 * Operaciones de negocio sobre el catálogo de razas.
 */
public interface RazaService {

    List<Raza> listarRazas();

    Raza buscarRazaPorNombre(String nombre) throws BadRequestException;

    MiRespuestaRS guardarRaza(RazaRq razaRq) throws BadRequestException;

    MiRespuestaRS actualizarRaza(RazaRq razaRq) throws BadRequestException;
}
