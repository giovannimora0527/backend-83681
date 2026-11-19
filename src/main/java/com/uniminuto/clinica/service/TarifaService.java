package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.TarifaRq;
import com.uniminuto.clinica.models.TarifaRs;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Define las operaciones sobre tarifas, cambios registrados e IVA de medicamentos. */
public interface TarifaService {
    /** Consulta los registros de la tarifa veterinaria. */
    List<TarifaRs> listar(Long especializacionId);
    /** Consulta las tarifas aplicables a la cita. */
    List<TarifaRs> listarParaCita(long citaId);
    /** Registra la tarifa veterinaria. */
    TarifaRs crear(TarifaRq solicitud, long usuarioId);
    /** Guarda los cambios de la tarifa veterinaria. */
    TarifaRs actualizar(long id, TarifaRq solicitud, long usuarioId);
    /** Consulta el historial de la tarifa veterinaria. */
    List<Map<String, Object>> historial(long id);
    /** Devuelve el porcentaje de IVA del medicamento indicado. */
    BigDecimal consultarIvaMedicamento(long id);
    /** Guarda el porcentaje de IVA asignado al medicamento. */
    void configurarIvaMedicamento(long id, BigDecimal porcentaje, long usuarioId);
}
