package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para AnotacionHistoria.
 */
@Repository
public interface AnotacionHistoriaRepository extends JpaRepository<AnotacionHistoria, Long> {

    /**
     * Recupera las anotaciones de una historia ordenadas por fecha (más recientes primero).
     * @param historia entidad HistoriaMedica utilizada como filtro
     * @return lista de anotaciones ordenadas descendentemente por fecha
     */
    List<AnotacionHistoria> findByHistoriaOrderByFechaDesc(HistoriaMedica historia);
}
