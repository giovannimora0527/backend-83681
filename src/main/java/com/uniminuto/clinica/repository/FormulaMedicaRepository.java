package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la gestion de formulas medicas en la base de datos.
 */
@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    /**
     * Consulta para obtener todas las formulas medicas ordenadas por fecha de creacion descendente.
     * @return Lista de formulas medicas ordenadas de la mas reciente a la mas antigua.
     */
    List<FormulaMedica> findAllByOrderByFechaCreacionDesc();
}