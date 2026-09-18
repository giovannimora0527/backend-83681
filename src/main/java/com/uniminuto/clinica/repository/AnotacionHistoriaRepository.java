package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnotacionHistoriaRepository extends JpaRepository<AnotacionHistoria, Long> {

    /**
     * Busca las anotaciones cuya fecha se encuentra entre fechaInicio y fechaFin,
     * ordenadas de la más reciente a la más antigua.
     *
     * @param fechaInicio fecha y hora inicial del rango a filtrar.
     * @param fechaFin    fecha y hora final del rango a filtrar.
     * @return lista de anotaciones dentro del rango, ordenada de forma descendente por fecha.
     */
    List<AnotacionHistoria> findAllByFechaBetweenOrderByFechaDesc(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}