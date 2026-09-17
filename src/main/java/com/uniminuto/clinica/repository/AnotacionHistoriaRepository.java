// repository/AnotacionHistoriaRepository.java
package com.uniminuto.clinica.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uniminuto.clinica.entity.AnotacionHistoria;

public interface AnotacionHistoriaRepository extends JpaRepository<AnotacionHistoria, Long> {

    /**
     * Lista las anotaciones de historia entre una fecha inicial y final,
     * ordenadas de la mas reciente a la mas antigua.
     */
    List<AnotacionHistoria> findByFechaBetweenOrderByFechaDesc(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}