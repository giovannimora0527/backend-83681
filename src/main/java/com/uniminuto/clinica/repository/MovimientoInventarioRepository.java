package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByLote_Medicamento_IdOrderByFechaDescIdDesc(Long medicamentoId);

    List<MovimientoInventario> findByFormula_IdOrderByFechaAscIdAsc(Long formulaId);
}
