package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.entity.FacturaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface FacturaDetalleRepository extends JpaRepository<FacturaDetalle, Long> {
    List<FacturaDetalle> findByFactura_IdOrderByIdAsc(Long facturaId);
    boolean existsByDespacho_Id(Long despachoId);
}
