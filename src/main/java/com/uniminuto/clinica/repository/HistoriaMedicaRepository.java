package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long> {

    /**
     * Lista todas las historias médicas ordenadas por su fecha de creación,
     * de la más reciente a la más antigua.
     *
     * @return lista de historias médicas ordenada de forma descendente por fecha_creacion.
     */
    List<HistoriaMedica> findAllByOrderByFechaCreacionDesc();
}