package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a datos de las fórmulas médicas.
 */
@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    /**
     * Lista todas las fórmulas ordenadas por fecha de creación, de la más reciente a la más antigua.
     *
     * @return fórmulas ordenadas descendentemente.
     */
    List<FormulaMedica> findAllByOrderByFechaCreacionDesc();
}
