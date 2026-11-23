package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.AnotacionHistoria;
<<<<<<< HEAD
import com.uniminuto.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnotacionHistoriaRepository extends JpaRepository<AnotacionHistoria, Long> {
    List<AnotacionHistoria> findByHistoria(HistoriaMedica historia);
    List<AnotacionHistoria> findByHistoria_Id(Long historiaId);
=======
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de anotaciones de historia clínica.
 */
@Repository
public interface AnotacionHistoriaRepository extends JpaRepository<AnotacionHistoria, Long>, JpaSpecificationExecutor<AnotacionHistoria> {

    /**
     * Lista las anotaciones de una historia, de la más reciente a la más antigua.
     *
     * @param historiaId identificador de la historia.
     * @return anotaciones ordenadas por fecha descendente.
     */
    List<AnotacionHistoria> findByHistoriaIdOrderByFechaHoraDesc(Long historiaId);

    /**
     * Lista las anotaciones de una historia dentro de un rango de fechas.
     *
     * @param historiaId identificador de la historia.
     * @param inicio     inicio del rango (inclusive).
     * @param fin        fin del rango (inclusive).
     * @return anotaciones del rango ordenadas por fecha descendente.
     */
    List<AnotacionHistoria> findByHistoriaIdAndFechaHoraBetweenOrderByFechaHoraDesc(
            Long historiaId, LocalDateTime inicio, LocalDateTime fin);
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
