package com.uniminuto.clinica.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uniminuto.clinica.entity.FormulaMedica;

public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    /**
     * Lista todas las formulas medicas ordenadas por fecha de creacion,
     * de la mas reciente a la mas antigua.
     *
     * @return lista de formulas medicas ordenada descendentemente.
     */
    List<FormulaMedica> findAllByOrderByFechaCreacionDesc();
}
