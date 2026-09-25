package com.uniminuto.veterinaria.service;

import com.uniminuto.veterinaria.entity.HistoriaMedica;
import java.util.List;

/**
 * Interfaz de la capa de servicio para definir las operaciones de negocio del recurso HistoriaMedica.
 */
public interface HistoriaMedicaService {

    List<HistoriaMedica> listarTodas();

    List<HistoriaMedica> filtrarPorFecha(String inicio, String fin);

    HistoriaMedica guardarHistoriaMedica(HistoriaMedica historiaMedica);

    HistoriaMedica actualizarHistoriaMedica(Long id, HistoriaMedica historiaMedica);
}