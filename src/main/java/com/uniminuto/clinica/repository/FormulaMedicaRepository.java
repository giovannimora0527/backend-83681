package com.uniminuto.clinica.repository;


import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {


    /**
     * Busca todas las fórmulas médicas ordenadas por fecha de creación de forma descendente.
     * @return Lista de fórmulas médicas ordenadas por fecha de creación descendente.
     */

        java.util.List<FormulaMedica> findAllByOrderByFechaCreacionRegistroDesc();

}
