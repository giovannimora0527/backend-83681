package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CuentaMedicoRs;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Define las operaciones del directorio veterinario y el vínculo de las cuentas con los médicos. */
public interface MedicoService {

    /** Consulta las cuentas que pueden asociarse a un veterinario. */
    List<CuentaMedicoRs> listarCuentasDisponibles();

    /** Consulta la cuenta de acceso asociada al veterinario. */
    Optional<CuentaMedicoRs> consultarCuenta(Long medicoId);

    /** Relaciona la cuenta seleccionada con el veterinario. */
    CuentaMedicoRs vincularCuenta(Long medicoId, Long usuarioId);

    /** Desasocia la cuenta de acceso del veterinario indicado. */
    void desvincularCuenta(Long medicoId);

    /** Consulta los veterinarios. */
    List<Medico> listarMedicos();

    /** Busca los veterinarios. */
    List<Medico> buscarMedicos(String termino);

    /** Consulta los veterinarios que tienen disponibilidad para la fecha indicada. */
    List<Medico> listarMedicosDisponiblesPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                                 Long citaId, Long mascotaId)
            throws BadRequestException;

    /** Guarda el veterinario. */
    MiRespuestaRS guardarMedico(MedicoRq medicoRq) throws BadRequestException;

    /** Actualiza el veterinario. */
    MiRespuestaRS actualizarMedico(MedicoRq medicoRq) throws BadRequestException;
}
