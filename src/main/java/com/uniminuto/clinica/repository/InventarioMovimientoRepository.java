package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.InventarioMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, Long> {
    List<InventarioMovimiento> findByLoteMedicamentoIdOrderByCreadoEnDesc(Long medicamentoId);
}
