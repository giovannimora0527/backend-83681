package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Busca las citas cuya fecha_hora se encuentra entre fechaInicio y fechaFin,
     * ordenadas de la más reciente a la más antigua.
     *
     * @param fechaInicio fecha y hora inicial del rango a filtrar.
     * @param fechaFin    fecha y hora final del rango a filtrar.
     * @return lista de citas dentro del rango, ordenada de forma descendente por fecha_hora.
     */
    List<Cita> findAllByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}