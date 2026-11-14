package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.DespachoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DespachoDetalleRepository extends JpaRepository<DespachoDetalle, Long> {

    List<DespachoDetalle> findByDespacho_IdOrderByIdAsc(Long despachoId);

    @Query("select coalesce(sum(det.cantidadEntregada), 0) "
            + "from DespachoDetalle det "
            + "where det.formulaDetalleId = :formulaDetalleId "
            + "and det.despacho.estado <> 'CANCELADO'")
    Long sumarCantidadEntregadaPorFormulaDetalle(
            @Param("formulaDetalleId") Long formulaDetalleId);
}
