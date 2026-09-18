package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de la entidad {@link AnotacionHistoria}.
 */
@Repository
public interface AnotacionHistoriaRepository extends JpaRepository<AnotacionHistoria, Long> {

    /**
     * Lista las anotaciones asociadas a una historia médica, ordenadas
     * de la más reciente a la más antigua.
     *
     * @param historiaId identificador de la historia médica.
     * @return lista de anotaciones de la historia médica.
     */
    List<AnotacionHistoria> findByHistoriaMedica_HistoriaIdOrderByFechaCreacionDesc(Long historiaId);
}
