package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.InventarioLote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Consultas y bloqueos transaccionales de los lotes de inventario. */
@Repository
public interface InventarioLoteRepository extends JpaRepository<InventarioLote, Long> {

    /** Lista los lotes por fecha de vencimiento. */
    List<InventarioLote> findAllByOrderByFechaVencimientoAsc();

    /** Lista los lotes de un medicamento por vencimiento. */
    List<InventarioLote> findByMedicamento_IdOrderByFechaVencimientoAsc(Long medicamentoId);

    /** Lista lotes vigentes y activos para consultar disponibilidad. */
    @Query("select l from InventarioLote l where l.medicamento.id = :medicamentoId "
            + "and l.activo = true and l.cantidad > 0 "
            + "and (l.fechaVencimiento is null or l.fechaVencimiento >= :fechaCorte) "
            + "order by case when l.fechaVencimiento is null then 1 else 0 end, l.fechaVencimiento asc, l.id asc")
    List<InventarioLote> buscarLotesDisponibles(@Param("medicamentoId") Long medicamentoId,
                                                @Param("fechaCorte") LocalDate fechaCorte);

    /** Lista lotes vigentes con existencias que vencen dentro de un rango. */
    @Query("select l from InventarioLote l where l.activo = true and l.cantidad > 0 "
            + "and l.fechaVencimiento between :desde and :hasta order by l.fechaVencimiento asc")
    List<InventarioLote> buscarLotesPorVencer(@Param("desde") LocalDate desde,
                                               @Param("hasta") LocalDate hasta);

    /** Comprueba si el producto ya tiene un lote con ese número. */
    boolean existsByMedicamento_IdAndNumeroLoteIgnoreCase(Long medicamentoId, String numeroLote);

    /** Comprueba si otro lote del mismo producto tiene ese número. */
    boolean existsByMedicamento_IdAndNumeroLoteIgnoreCaseAndIdNot(
            Long medicamentoId,
            String numeroLote,
            Long id);

    /** Busca y bloquea los lotes vigentes en orden de vencimiento. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from InventarioLote l "
            + "where l.medicamento.id = :medicamentoId "
            + "and l.activo = true and l.cantidad > 0 "
            + "and (l.fechaVencimiento is null or l.fechaVencimiento >= :fechaCorte) "
            + "order by case when l.fechaVencimiento is null then 1 else 0 end, l.fechaVencimiento asc, l.id asc")
    List<InventarioLote> buscarDisponiblesPorVencimientoParaBloqueo(
            @Param("medicamentoId") Long medicamentoId,
            @Param("fechaCorte") LocalDate fechaCorte);

    /** Obtiene el registro por identificador con bloqueo durante la operación. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from InventarioLote l where l.id = :id")
    Optional<InventarioLote> findLockedById(@Param("id") Long id);
}
