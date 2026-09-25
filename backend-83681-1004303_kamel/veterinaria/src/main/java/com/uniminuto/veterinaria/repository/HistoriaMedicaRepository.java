package com.uniminuto.veterinaria.repository;

import com.uniminuto.veterinaria.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para el acceso a datos de la entidad HistoriaMedica.
 */
@Repository
public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long> {

    /**
     * Obtiene historias medicas en un rango de fechas ordenadas de la mas reciente a la antigua.
     * @param inicio Fecha inicial del filtro.
     * @param fin Fecha final del filtro.
     * @return Lista de historias medicas ordenadas.
     */
    List<HistoriaMedica> findByFechaBetweenOrderByFechaDesc(LocalDate inicio, LocalDate fin);
}