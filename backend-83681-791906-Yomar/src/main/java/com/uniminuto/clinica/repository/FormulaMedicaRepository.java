package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    /**
     * Lista todas las fórmulas médicas ordenadas por su fecha de creación,
     * desde la más reciente hasta la más antigua.
     *
     * @return lista de fórmulas médicas ordenadas de forma descendente por fecha de creación.
     */
    List<FormulaMedica> findAllByOrderByFechaCreacionRegistroDesc();
}