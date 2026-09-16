package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cita;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaz de servicio para la gestión de citas médicas.
 */
public interface CitaService {

    /**
     * Filtra las citas entre dos fechas ordenadas por fecha descendente.
     * 
     * @param fechaInicio Fecha inicial del filtro.
     * @param fechaFin Fecha final del filtro.
     * @return Lista de citas filtradas.
     */
    List<Cita> filtrarPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Crea una nueva cita en el sistema.
     * 
     * @param cita Entidad cita a guardar.
     * @return La cita creada.
     */
    Cita guardarCita(Cita cita);

    /**
     * Actualiza una cita existente.
     * 
     * @param id Identificador de la cita.
     * @param cita Detalles de la cita a actualizar.
     * @return La cita actualizada.
     */
    Cita actualizarCita(Long id, Cita cita);
}