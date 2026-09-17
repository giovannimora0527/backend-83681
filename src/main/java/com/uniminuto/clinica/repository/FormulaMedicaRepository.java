package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad FormulaMedica.
 */
@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    /**
     * Lista todas las formulas medicas del inventario ordenadas por fecha de
     * creacion, de la mas reciente a la mas antigua.
     *
     * @return Lista de formulas medicas ordenadas descendentemente.
     */
    List<FormulaMedica> findAllByOrderByFechaCreacionDesc();
}
