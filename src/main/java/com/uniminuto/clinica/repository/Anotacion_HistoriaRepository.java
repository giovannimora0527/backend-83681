package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Anotacion_Historia;
import com.uniminuto.clinica.entity.Historia_Medica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para Anotacion_Historia.
 */
@Repository
public interface Anotacion_HistoriaRepository extends JpaRepository<Anotacion_Historia, Long> {

    /**
     * Recupera las anotaciones de una historia ordenadas por fecha (más recientes primero).
     *
     * @param historia entidad HistoriaMedica utilizada como filtro
     * @return lista de anotaciones ordenadas descendentemente por fecha
     */
    List<Anotacion_Historia> findByHistoriaOrderByFechaDesc(Historia_Medica historia);

    /**
     * Recupera las anotaciones de una historia dentro de un rango de fechas.
     *
     * @param historia entidad HistoriaMedica utilizada como filtro
     * @param fechaInicial fecha inicial del rango (inclusive)
     * @param fechaFinal fecha final del rango (inclusive)
     * @return lista de anotaciones ordenadas por fecha descendente
     */
    List<Anotacion_Historia> findByHistoriaAndFechaBetweenOrderByFechaDesc(Historia_Medica historia, LocalDateTime fechaInicial, LocalDateTime fechaFinal);
}
