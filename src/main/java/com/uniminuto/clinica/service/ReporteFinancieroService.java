package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.ReporteRs;

import java.time.LocalDate;

/** Define el resumen de facturación y recaudo por día, mes o año dentro de un rango. */
public interface ReporteFinancieroService {

    /** Consulta el resumen de facturación y recaudo. */
    ReporteRs consultar(LocalDate inicio, LocalDate fin, String periodo, Long medico, Long especialidad);
}
