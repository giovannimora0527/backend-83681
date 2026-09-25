package com.uniminuto.veterinaria.repository;

import com.uniminuto.veterinaria.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad FormulaMedica.
 */
@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    /**
     * Consulta todas las fórmulas médicas ordenadas por fecha de creación descendente.
     *
     * @return Lista de fórmulas médicas.
     */
    List<FormulaMedica> findAllByOrderByFechaCreacionDesc();
}