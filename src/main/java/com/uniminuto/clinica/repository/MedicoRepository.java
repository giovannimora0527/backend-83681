package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

<<<<<<< HEAD
import java.time.LocalDateTime;
import java.util.List;
=======
import java.util.List;
import java.util.Optional;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661

/**
 * Repositorio de médicos.
 */
@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

<<<<<<< HEAD
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
=======
    /**
     * Busca un médico por tipo y número de documento.
     *
     * @param tipoDocumento   tipo de documento.
     * @param numeroDocumento número de documento.
     * @return médico encontrado, si existe.
     */
    Optional<Medico> findByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);

    /**
     * Busca un médico por registro profesional.
     *
     * @param registroProfesional registro a buscar.
     * @return médico encontrado, si existe.
     */
    Optional<Medico> findByRegistroProfesional(String registroProfesional);

    /**
     * Lista los médicos ordenados por apellidos y nombres.
     *
     * @return lista ordenada.
     */
    List<Medico> findAllByOrderByApellidosAscNombresAsc();
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
