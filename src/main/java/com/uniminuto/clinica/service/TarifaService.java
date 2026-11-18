package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.TarifaRq;
import com.uniminuto.clinica.models.TarifaRs;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Define las operaciones sobre tarifas, cambios registrados e IVA de medicamentos. */
public interface TarifaService {
    List<TarifaRs> listar(Long especializacionId);
    List<TarifaRs> listarParaCita(long citaId);
    TarifaRs crear(TarifaRq solicitud, long usuarioId);
    TarifaRs actualizar(long id, TarifaRq solicitud, long usuarioId);
    List<Map<String, Object>> historial(long id);
    void configurarIvaMedicamento(long id, BigDecimal porcentaje, long usuarioId);
}
