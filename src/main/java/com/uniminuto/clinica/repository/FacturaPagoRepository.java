package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.entity.FacturaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.math.BigDecimal;
public interface FacturaPagoRepository extends JpaRepository<FacturaPago,Long> {
    List<FacturaPago> findByFacturaIdOrderByPagadoEnAsc(Long facturaId);
    List<FacturaPago> findByPagadoEnBetweenOrderByPagadoEnAsc(java.time.LocalDateTime desde, java.time.LocalDateTime hasta);
    boolean existsByFacturaId(Long facturaId);
    @org.springframework.data.jpa.repository.Query("select coalesce(sum(p.monto),0) from FacturaPago p where p.facturaId=:id") BigDecimal totalByFactura(@org.springframework.data.repository.query.Param("id") Long id);
}
