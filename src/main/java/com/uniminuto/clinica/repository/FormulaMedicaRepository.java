package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio (acceso a base de datos) de las formulas medicas.
 */
@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    /**
     * Lista las formulas de la mas reciente a la mas antigua (por fecha de creacion).
     *
     * @return formulas ordenadas descendentemente.
     */
    List<FormulaMedica> findAllByOrderByFechaCreacionDesc();

    /**
     * Lista las formulas de la mas antigua a la mas reciente (por fecha de creacion).
     *
     * @return formulas ordenadas ascendentemente.
     */
    List<FormulaMedica> findAllByOrderByFechaCreacionAsc();
}
