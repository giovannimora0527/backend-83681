package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Acceso a datos de las anotaciones de historia clínica.
 */
@Repository
public interface AnotacionHistoriaRepository extends JpaRepository<AnotacionHistoria, Long> {

    /**
     * Lista las anotaciones de una historia, de la más reciente a la más antigua.
     *
     * @param historiaId identificador de la historia clínica.
     * @return anotaciones de la historia.
     */
    List<AnotacionHistoria> findByHistoriaMedica_IdOrderByFechaDesc(Long historiaId);

    /**
     * Lista las anotaciones entre dos fechas, de la más reciente a la más antigua.
     *
     * @param fechaInicio fecha inicial.
     * @param fechaFin fecha final.
     * @return anotaciones del rango.
     */
    List<AnotacionHistoria> findByFechaBetweenOrderByFechaDesc(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
