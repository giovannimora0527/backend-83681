package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.CargaMasivaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.Optional;

/** Acceso a los registros de prevalidación de cargas masivas. */
@Repository
public interface CargaMasivaLogRepository extends JpaRepository<CargaMasivaLog, Long> {

    /** Bloquea una carga para impedir confirmaciones simultáneas. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CargaMasivaLog c where c.id = :id")
    Optional<CargaMasivaLog> findLockedById(@Param("id") Long id);
}
