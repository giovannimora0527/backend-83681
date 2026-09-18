package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de la entidad {@link FormulaMedica}.
 */
@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    /**
     * Lista todas las fórmulas médicas del inventario ordenadas por fecha
     * de creación, de la más reciente a la más antigua.
     *
     * @return lista de fórmulas médicas ordenadas descendentemente.
     */
    List<FormulaMedica> findAllByOrderByFechaCreacionDesc();
}
