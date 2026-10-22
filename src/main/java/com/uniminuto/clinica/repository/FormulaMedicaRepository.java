package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de fórmulas médicas. Incluye consultas dinámicas para el listado paginado.
 */
@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long>,
        JpaSpecificationExecutor<FormulaMedica> {

    /**
     * Lista las fórmulas de la más reciente a la más antigua por fecha de creación.
     *
     * @return fórmulas ordenadas por fecha de creación descendente.
     */
    List<FormulaMedica> findAllByOrderByFechaCreacionRegistroDesc();

    /**
     * Lista las fórmulas de una mascota, de la más reciente a la más antigua.
     *
     * @param mascotaId identificador de la mascota.
     * @return fórmulas de la mascota ordenadas por fecha de creación descendente.
     */
    List<FormulaMedica> findByMascotaMascotaIdOrderByFechaCreacionRegistroDesc(Integer mascotaId);
}
