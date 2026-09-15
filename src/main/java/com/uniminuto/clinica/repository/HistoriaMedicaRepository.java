package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para la entidad HistoriaMedica.
 * Provee métodos para recuperar historias ordenadas por fecha.
 */
@Repository
public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long> {

    /**
     * Obtiene todas las historias ordenadas de la más reciente a la más antigua.
     * @return lista de historias ordenadas descendentemente por fechaCreacion
     */
    List<HistoriaMedica> findAllByOrderByFechaCreacionDesc();

    /**
     * Obtiene todas las historias cuya fechaCreacion está entre inicio y fin, ordenadas descendentemente.
     * @param inicio fecha inicial inclusive
     * @param fin fecha final inclusive
     * @return lista de historias dentro del rango ordenadas descendentemente
     */
    List<HistoriaMedica> findAllByFechaCreacionBetweenOrderByFechaCreacionDesc(LocalDateTime inicio, LocalDateTime fin);
}
