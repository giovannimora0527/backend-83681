package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long> {

    /**
     * Busca las historias médicas cuya fecha de creación esté entre una
     * fecha inicial y una fecha final (ambas inclusive), ordenadas desde
     * la más reciente hasta la más antigua.
     *
     * @param fechaInicial fecha inicial del rango de búsqueda.
     * @param fechaFinal   fecha final del rango de búsqueda.
     * @return lista de historias médicas dentro del rango, ordenada de
     *         forma descendente.
     */
    List<HistoriaMedica> findByFechaCreacionBetweenOrderByFechaCreacionDesc(LocalDateTime fechaInicial, LocalDateTime fechaFinal);
}