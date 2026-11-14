package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/** Acceso al historial de movimientos de inventario. */
@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    /** Consulta los movimientos de un producto en orden cronológico descendente. */
    List<MovimientoInventario> findByMedicamento_IdOrderByFechaMovimientoDescIdDesc(Long medicamentoId);

    /** Consulta los movimientos de todos los productos en orden cronológico descendente. */
    List<MovimientoInventario> findAllByOrderByFechaMovimientoDescIdDesc();

    /** Encuentra la salida asociada con un detalle de despacho. */
    Optional<MovimientoInventario> findByDespachoDetalle_Id(Long despachoDetalleId);
}
