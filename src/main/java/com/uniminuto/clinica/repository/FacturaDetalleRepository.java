package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.entity.FacturaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface FacturaDetalleRepository extends JpaRepository<FacturaDetalle,Long> {
    List<FacturaDetalle> findByFacturaIdOrderByIdAsc(Long facturaId);
    boolean existsByDespachoDetalleId(Long despachoDetalleId);
    @org.springframework.data.jpa.repository.Query("select count(d) > 0 from FacturaDetalle d, Factura f where d.facturaId=f.id and d.despachoDetalleId=:detalleId and f.estado <> 'ANULADA'")
    boolean existeEnFacturaActiva(@org.springframework.data.repository.query.Param("detalleId") Long detalleId);
}
