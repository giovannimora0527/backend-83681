package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.entity.DespachoHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DespachoHistorialRepository extends JpaRepository<DespachoHistorial, Long> {
    java.util.List<DespachoHistorial> findAllByOrderByCreadoEnDesc();
    java.util.List<DespachoHistorial> findByDespachoIdOrderByCreadoEnAsc(Long despachoId);
}
