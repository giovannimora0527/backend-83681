package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    List<FormulaMedica> findAllByOrderByFechaCreacionRegistroAsc();

    List<FormulaMedica> findByCita_IdInOrderByFechaCreacionRegistroDesc(List<Long> citaIds);

    List<FormulaMedica> findAllByOrderByFechaCreacionRegistroDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FormulaMedica f join fetch f.medicamento where f.id = :id")
    java.util.Optional<FormulaMedica> findByIdForUpdate(@Param("id") Long id);

    List<FormulaMedica> findByEstadoDispensacionOrderByFechaCreacionRegistroAsc(String estado);

    List<FormulaMedica> findByEstadoDispensacionInOrderByFechaCreacionRegistroAsc(List<String> estados);
}
