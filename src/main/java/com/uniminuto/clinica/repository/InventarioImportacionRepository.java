package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.entity.InventarioImportacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface InventarioImportacionRepository extends JpaRepository<InventarioImportacion, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventarioImportacion i where i.id = :id")
    Optional<InventarioImportacion> findLockedById(@Param("id") Long id);
}
