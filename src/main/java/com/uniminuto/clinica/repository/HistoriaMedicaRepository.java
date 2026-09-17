package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad HistoriaMedica.
 */
@Repository
public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long> {

    /**
     * Lista todas las historias medicas ordenadas de la mas reciente a la mas
     * antigua.
     *
     * @return Lista de historias medicas ordenadas descendentemente.
     */
    List<HistoriaMedica> findAllByOrderByFechaCreacionDesc();

    /**
     * Filtra las historias medicas creadas dentro del rango de fechas recibido
     * y las ordena de la mas reciente a la mas antigua.
     *
     * @param fechaInicial Limite inferior del rango de busqueda.
     * @param fechaFinal Limite superior del rango de busqueda.
     * @return Lista de historias medicas del rango ordenadas descendentemente.
     */
    List<HistoriaMedica> findByFechaCreacionBetweenOrderByFechaCreacionDesc(
            LocalDateTime fechaInicial, LocalDateTime fechaFinal);
}
