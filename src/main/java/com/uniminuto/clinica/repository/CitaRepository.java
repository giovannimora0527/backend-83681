package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository

public interface CitaRepository extends JpaRepository<Cita, Long> {
    /**
     * Filtra las citas en un rango de fechas y las ordena de la más reciente a la más antigua.
     * @param fechaInicio Fecha inicial del rango.
     * @param fechaFin Fecha final del rango.
     * @return Lista de citas encontradas.
     */
    List<Cita> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
