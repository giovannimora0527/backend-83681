package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.entity.Despacho;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface DespachoRepository extends JpaRepository<Despacho, Long> {
    java.util.List<Despacho> findAllByOrderByCreadoEnDesc();
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select d from Despacho d where d.id = :id")
    java.util.Optional<Despacho> findLockedById(@org.springframework.data.repository.query.Param("id") Long id);
    Optional<Despacho> findByIdempotencyKey(String idempotencyKey);
}
