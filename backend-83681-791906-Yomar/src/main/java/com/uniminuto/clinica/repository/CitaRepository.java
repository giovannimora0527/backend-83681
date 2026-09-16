package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Busca las citas cuya fecha y hora esté entre una fecha inicial y una
     * fecha final (ambas inclusive), ordenadas desde la más reciente hasta
     * la más antigua.
     *
     * @param fechaInicial fecha inicial del rango de búsqueda.
     * @param fechaFinal   fecha final del rango de búsqueda.
     * @return lista de citas dentro del rango, ordenada de forma descendente.
     */
    List<Cita> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime fechaInicial, LocalDateTime fechaFinal);
}