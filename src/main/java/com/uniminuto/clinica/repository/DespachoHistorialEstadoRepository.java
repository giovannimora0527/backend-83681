package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.DespachoHistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DespachoHistorialEstadoRepository extends JpaRepository<DespachoHistorialEstado, Long> {

    List<DespachoHistorialEstado> findByDespacho_IdOrderByFechaCambioAsc(Long despachoId);
}
