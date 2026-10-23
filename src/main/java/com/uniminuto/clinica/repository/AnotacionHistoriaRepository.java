package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio (acceso a base de datos) de las anotaciones de historia clinica.
 */
@Repository
public interface AnotacionHistoriaRepository extends JpaRepository<AnotacionHistoria, Long> {

    /**
     * Lista las anotaciones de una historia, de la mas reciente a la mas antigua.
     *
     * @param historiaId id de la historia clinica.
     * @return anotaciones de la historia.
     */
    List<AnotacionHistoria> findByHistoriaIdOrderByFechaDesc(Long historiaId);

    /**
     * Lista todas las anotaciones de una mascota (de todas sus historias),
     * de la mas reciente a la mas antigua.
     *
     * @param mascotaId id de la mascota.
     * @return historial completo de anotaciones de la mascota.
     */
    List<AnotacionHistoria> findByHistoriaMascotaMascotaIdOrderByFechaDesc(Long mascotaId);
}
