package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.DespachoHistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Acceso al historial inmutable de cambios de estado de los despachos. */
@Repository
public interface DespachoHistorialEstadoRepository extends JpaRepository<DespachoHistorialEstado, Long> {

    /** Consulta los cambios de estado del despacho en el orden en que ocurrieron. */
    List<DespachoHistorialEstado> findByDespacho_IdOrderByFechaCambioAsc(Long despachoId);
}
