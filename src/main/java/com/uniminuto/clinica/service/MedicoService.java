package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Operaciones de negocio sobre médicos.
 */
public interface MedicoService {

    List<Medico> listarMedicos();

<<<<<<< HEAD
    List<Medico> listarMedicosDisponiblesPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin)
            throws BadRequestException;

    MiRespuestaRS guardarMedico(MedicoRq medicoRq) throws BadRequestException;

=======
    Medico buscarMedico(String tipoDocumento, String numeroDocumento) throws BadRequestException;

    Medico buscarMedicoPorRegistroProfesional(String registroProfesional) throws BadRequestException;

    MiRespuestaRS guardarMedico(MedicoRq medicoRq) throws BadRequestException;

>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
    MiRespuestaRS actualizarMedico(MedicoRq medicoRq) throws BadRequestException;
}
