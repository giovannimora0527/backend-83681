package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.AtencionDetalleRs;
import com.uniminuto.clinica.models.AtencionRq;
import com.uniminuto.clinica.models.PrescripcionRq;
import java.io.IOException;

/**
 * Define la consulta de antecedentes, la formulación y el cierre de la atención que genera la
 * prefactura.
 */
public interface AtencionService {
    /** Reúne los datos de la cita y las historias anteriores del paciente. */
    AtencionDetalleRs consultar(long citaId);
    /** Cierra la atención y devuelve el identificador de la prefactura. */
    long finalizar(long citaId, AtencionRq solicitud, long usuarioId);
    /** Guarda una fórmula con todos los medicamentos solicitados. */
    long formular(long citaId, PrescripcionRq solicitud);
    /** Obtiene el saldo disponible de un medicamento para la atención. */
    long existencias(long citaId, long medicamentoId);
    /** Prepara los datos del documento PDF de la fórmula. */
    byte[] formula(long citaId, long formulaId) throws IOException;
}
