package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Historia_Medica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para la entidad HistoriaMedica.
 * Provee métodos para recuperar historias ordenadas por fecha.
 */
@Repository
public interface Historia_MedicaRepository extends JpaRepository<Historia_Medica, Long> {

    /**
     * Obtiene todas las historias ordenadas de la más reciente a la más antigua.
     * @return lista de historias ordenadas descendentemente por fechaCreacion
     */
    List<Historia_Medica> findAllByOrderByFechaCreacionDesc();

    /**
     * Obtiene todas las historias cuya fechaCreacion está entre inicio y fin, ordenadas descendentemente.
     * @param inicio fecha inicial inclusive
     * @param fin fecha final inclusive
     * @return lista de historias dentro del rango ordenadas descendentemente
     */
    List<Historia_Medica> findAllByFechaCreacionBetweenOrderByFechaCreacionDesc(LocalDateTime inicio, LocalDateTime fin);
}
