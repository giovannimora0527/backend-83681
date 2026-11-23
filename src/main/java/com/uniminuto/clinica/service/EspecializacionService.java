package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
<<<<<<< HEAD
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.web.bind.annotation.RequestBody;
=======
import com.uniminuto.clinica.models.EspecializacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661

import java.util.List;

/**
 * Operaciones de negocio sobre el catálogo de especializaciones.
 */
public interface EspecializacionService {

    List<Especializacion> listarEspecializaciones();

<<<<<<< HEAD
    MiRespuestaRS crearEspecializacion(Especializacion especializacion)
            throws BadRequestException;

    MiRespuestaRS actualizarEspecializacion(Especializacion especializacion)
            throws BadRequestException;
=======
    Especializacion buscarEspecializacionPorCodigo(String codigo) throws BadRequestException;

    MiRespuestaRS guardarEspecializacion(EspecializacionRq especializacionRq) throws BadRequestException;

    MiRespuestaRS actualizarEspecializacion(EspecializacionRq especializacionRq) throws BadRequestException;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
