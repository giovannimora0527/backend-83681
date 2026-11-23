package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.LoteMedicamento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface LoteMedicamentoRepository extends JpaRepository<LoteMedicamento, Long> {

    List<LoteMedicamento> findByMedicamento_IdOrderByFechaVencimientoAscIdAsc(Long medicamentoId);

    List<LoteMedicamento> findByMedicamento_IdInOrderByFechaVencimientoAscIdAsc(Collection<Long> medicamentoIds);

    boolean existsByMedicamento_IdAndCodigoLoteIgnoreCase(Long medicamentoId, String codigoLote);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LoteMedicamento l where l.medicamento.id = :medicamentoId "
            + "and l.cantidadDisponible > 0 and l.fechaVencimiento >= :hoy "
            + "order by l.fechaVencimiento asc, l.id asc")
    List<LoteMedicamento> findDispensablesFefoForUpdate(
            @Param("medicamentoId") Long medicamentoId,
            @Param("hoy") LocalDate hoy);
}
