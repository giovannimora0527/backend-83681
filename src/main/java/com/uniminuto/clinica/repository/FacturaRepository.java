package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.entity.Factura;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface FacturaRepository extends JpaRepository<Factura,Long> {
    Optional<Factura> findByAtencionId(Long atencionId);
    List<Factura> findByClienteIdOrderByEmitidaEnDesc(Long clienteId);
    List<Factura> findByMascotaIdOrderByEmitidaEnDesc(Integer mascotaId);
    List<Factura> findByEmitidaEnBetweenOrderByEmitidaEnAsc(java.time.LocalDateTime desde, java.time.LocalDateTime hasta);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select f from Factura f where f.id=:id") Optional<Factura> findLockedById(@Param("id") Long id);
}
