package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.ReporteRs;

import java.time.LocalDate;

/** Define la consulta consolidada de agenda y atenciones médicas en un rango de fechas. */
public interface ReporteMedicoService {

    ReporteRs consultar(LocalDate inicio, LocalDate fin, Long medicoId, Long especializacionId);
}
