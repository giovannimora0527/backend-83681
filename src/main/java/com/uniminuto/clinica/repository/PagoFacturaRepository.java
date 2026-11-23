package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.entity.PagoFactura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
public interface PagoFacturaRepository extends JpaRepository<PagoFactura, Long> {
    List<PagoFactura> findByFactura_IdOrderByFechaAscIdAsc(Long facturaId);
    @Query("select coalesce(sum(p.monto), 0) from PagoFactura p where p.factura.id = :id")
    BigDecimal totalPagado(@Param("id") Long facturaId);
}
