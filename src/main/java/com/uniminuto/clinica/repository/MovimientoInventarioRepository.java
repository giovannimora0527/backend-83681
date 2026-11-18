package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByMedicamentoIdOrderByFechaMovimientoDesc(Long medicamentoId);

    List<MovimientoInventario> findByFormulaMedicaIdOrderByFechaMovimientoDesc(Long formulaId);

    List<MovimientoInventario> findByUsuarioIdOrderByFechaMovimientoDesc(Long usuarioId);
}