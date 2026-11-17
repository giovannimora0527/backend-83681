package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.DespachoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Consultas de las líneas entregadas y acumulados de las fórmulas. */
@Repository
public interface DespachoDetalleRepository extends JpaRepository<DespachoDetalle, Long> {

    /** Lista las líneas de despacho ordenadas por su identificador. */
    List<DespachoDetalle> findByDespacho_IdOrderByIdAsc(Long despachoId);

    /** Consulta el historial de entregas de un medicamento con su despacho asociado. */
    @Query("select det from DespachoDetalle det where det.medicamento.id = :medicamentoId "
            + "order by det.despacho.fechaDespacho desc, det.id desc")
    List<DespachoDetalle> buscarHistorialPorMedicamento(@Param("medicamentoId") Long medicamentoId);

    /** Suma entregas válidas de una línea prescrita, excluyendo despachos cancelados. */
    @Query("select coalesce(sum(det.cantidadEntregada), 0) "
            + "from DespachoDetalle det "
            + "where det.formulaDetalleId = :formulaDetalleId "
            + "and det.despacho.estado <> 'CANCELADO'")
    Long sumarCantidadEntregadaPorFormulaDetalle(
            @Param("formulaDetalleId") Long formulaDetalleId);
}
