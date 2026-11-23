package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
<<<<<<< HEAD
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.RazaRq;
=======
import com.uniminuto.clinica.models.RazaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661

import java.util.List;

/**
 * Operaciones de negocio sobre el catálogo de razas.
 */
public interface RazaService {

<<<<<<< HEAD
    MiRespuestaRS crearRazaNueva(RazaRq razaRq) throws BadRequestException;

    MiRespuestaRS actualizarRaza(RazaRq razaRq) throws BadRequestException;

    List<Raza> listarRazas() throws BadRequestException;
=======
    List<Raza> listarRazas();

    Raza buscarRazaPorNombre(String nombre) throws BadRequestException;

    MiRespuestaRS guardarRaza(RazaRq razaRq) throws BadRequestException;

    MiRespuestaRS actualizarRaza(RazaRq razaRq) throws BadRequestException;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
