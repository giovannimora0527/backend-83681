package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad AnotacionHistoria.
 */
@Repository
public interface AnotacionHistoriaRepository extends JpaRepository<AnotacionHistoria, Long> {

    /**
     * Lista todas las anotaciones ordenadas de la mas reciente a la mas antigua.
     *
     * @return Lista de anotaciones ordenadas descendentemente.
     */
    List<AnotacionHistoria> findAllByOrderByFechaCreacionDesc();

    /**
     * Lista las anotaciones que pertenecen a una historia medica puntual,
     * ordenadas de la mas reciente a la mas antigua.
     *
     * @param historiaId Identificador de la historia medica.
     * @return Lista de anotaciones de la historia ordenadas descendentemente.
     */
    List<AnotacionHistoria> findByHistoriaMedicaHistoriaIdOrderByFechaCreacionDesc(
            Long historiaId);
}
