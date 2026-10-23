package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio (acceso a base de datos) de las historias clinicas.
 */
@Repository
public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long> {

    /**
     * Lista todas las historias, de la consulta mas reciente a la mas antigua.
     *
     * @return historias ordenadas por fecha de consulta descendente.
     */
    List<HistoriaMedica> findAllByOrderByFechaConsultaDesc();

    /**
     * Indica si una cita ya tiene historia clinica (evita duplicados).
     *
     * @param citaId id de la cita.
     * @return true si ya existe una historia para esa cita.
     */
    boolean existsByCitaId(Long citaId);
}
