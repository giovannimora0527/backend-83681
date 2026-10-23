package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

public interface MedicoService {

    List<Medico> listarMedicos();

    MiRespuestaRS guardarMedico(MedicoRq medicoRq);

    MiRespuestaRS actualizarMedico(MedicoRq medicoRq);

    MiRespuestaRS eliminarMedico(Long id);

    Medico buscarMedico(String tipoDocumento, String numeroDocumento)
            throws BadRequestException;

    Medico buscarMedicoPorRegistroProfesional(String registroProfesional)
            throws BadRequestException;
}
