package com.uniminuto.veterinaria.repository;

import com.uniminuto.veterinaria.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para el acceso a datos de la entidad Cita.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Obtiene citas en un rango de fechas ordenadas de la mas reciente a la antigua.
     * @param inicio Fecha inicial del filtro.
     * @param fin Fecha final del filtro.
     * @return Lista de citas ordenadas.
     */
    List<Cita> findByFechaBetweenOrderByFechaDesc(LocalDate inicio, LocalDate fin);
}