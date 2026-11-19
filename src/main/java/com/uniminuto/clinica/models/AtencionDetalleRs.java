package com.uniminuto.clinica.models;

import java.util.List;

/** Datos de la cita, antecedentes del paciente e historia clínica de la atención en curso. */
public record AtencionDetalleRs(CitaCalendarioRs cita, List<HistoriaAtencionRs> antecedentes,
                                HistoriaAtencionRs historiaActual, Long prefacturaId) {
}
