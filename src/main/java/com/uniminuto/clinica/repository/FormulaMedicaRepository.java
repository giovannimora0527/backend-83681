package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repositorio de fórmulas médicas. Incluye consultas dinámicas para el listado paginado.
 */
@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long>,
        JpaSpecificationExecutor<FormulaMedica> {

<<<<<<< HEAD
    List<FormulaMedica> findAllByOrderByFechaCreacionRegistroAsc();

    List<FormulaMedica> findByCita_IdInOrderByFechaCreacionRegistroDesc(List<Long> citaIds);

    List<FormulaMedica> findAllByOrderByFechaCreacionRegistroDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FormulaMedica f join fetch f.medicamento where f.id = :id")
    java.util.Optional<FormulaMedica> findByIdForUpdate(@Param("id") Long id);

    List<FormulaMedica> findByEstadoDispensacionOrderByFechaCreacionRegistroAsc(String estado);

    List<FormulaMedica> findByEstadoDispensacionInOrderByFechaCreacionRegistroAsc(List<String> estados);
=======
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
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
