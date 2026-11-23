package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.entity.Factura;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    boolean existsByCita_Id(Long citaId);
    Optional<Factura> findByCita_Id(Long citaId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Factura f join fetch f.cita where f.id = :id")
    Optional<Factura> findByIdForUpdate(@Param("id") Long id);
    List<Factura> findByFechaCreacionBetweenOrderByFechaCreacionAsc(LocalDateTime inicio, LocalDateTime fin);
}
