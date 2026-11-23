package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Medico m where m.id = :id")
    java.util.Optional<Medico> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT m FROM Medico m
            WHERE m.estado = true
              AND NOT EXISTS (
                SELECT c.id FROM Cita c
                WHERE c.medico = m
                  AND c.estado = :estado
                  AND c.fechaHora BETWEEN :fechaInicio AND :fechaFin
            )
            """)
    List<Medico> findDisponiblesPorFecha(@Param("fechaInicio") LocalDateTime fechaInicio,
                                        @Param("fechaFin") LocalDateTime fechaFin,
                                        @Param("estado") String estado);

    boolean existsByNumeroDocumento(String numeroDocumento);

    boolean existsByRegistroProfesional(String registroProfesional);

    boolean existsByNumeroDocumentoAndIdNot(String numeroDocumento, Long id);

    boolean existsByRegistroProfesionalAndIdNot(String registroProfesional, Long id);
}
