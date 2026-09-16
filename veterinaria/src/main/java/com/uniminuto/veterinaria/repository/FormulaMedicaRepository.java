package com.uniminuto.veterinaria.repository;

import com.uniminuto.veterinaria.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio para el acceso a datos de la entidad FormulaMedica.
 */
@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    /**
     * Obtiene formulas medicas ordenadas de la mas reciente a la mas antigua.
     * @return Lista de formulas medicas ordenadas.
     */
    List<FormulaMedica> findAllByOrderByFechaCreacionDesc();
}