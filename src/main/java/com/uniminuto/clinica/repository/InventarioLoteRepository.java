package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.InventarioLote;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InventarioLoteRepository extends JpaRepository<InventarioLote, Long> {
    List<InventarioLote> findByMedicamentoIdOrderByFechaVencimientoAsc(Long medicamentoId);
    List<InventarioLote> findByFechaVencimientoBetweenOrderByFechaVencimientoAsc(LocalDate desde, LocalDate hasta);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from InventarioLote l where l.id = :id")
    Optional<InventarioLote> findLockedById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from InventarioLote l where l.medicamento.id = :medicamentoId " +
            "and l.fechaVencimiento >= :hoy and l.cantidadDisponible > 0 order by l.fechaVencimiento asc, l.id asc")
    List<InventarioLote> findDisponiblesFefoBloqueados(@Param("medicamentoId") Long medicamentoId, @Param("hoy") LocalDate hoy);
}
