package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de la entidad {@link HistoriaMedica}.
 */
@Repository
public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long> {

    /**
     * Busca las historias médicas creadas entre una fecha inicial y una
     * fecha final, ordenadas de la más reciente a la más antigua.
     *
     * @param fechaInicial fecha inicial del rango de búsqueda.
     * @param fechaFinal   fecha final del rango de búsqueda.
     * @return lista de historias médicas encontradas en el rango, ordenadas descendentemente.
     */
    List<HistoriaMedica> findByFechaCreacionBetweenOrderByFechaCreacionDesc(LocalDateTime fechaInicial, LocalDateTime fechaFinal);
}
