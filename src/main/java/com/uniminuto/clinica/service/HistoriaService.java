package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.HistoriaMedica;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio que expone operaciones sobre historias médicas.
 */
public interface HistoriaService {

    /**
     * Filtra historias por un rango de fechas. Si inicio o fin son nulos, devuelve todas las historias
     * ordenadas de la más reciente a la más antigua.
     * @param inicio fecha/hora inicial del rango (inclusive) o null
     * @param fin fecha/hora final del rango (inclusive) o null
     * @return lista de historias que cumplen el filtro
     */
    List<HistoriaMedica> filtrarHistorias(LocalDateTime inicio, LocalDateTime fin);
}
