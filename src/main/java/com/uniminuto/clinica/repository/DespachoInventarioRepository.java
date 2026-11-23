package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.DespachoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DespachoInventarioRepository extends JpaRepository<DespachoInventario, Long> {
    Optional<DespachoInventario> findByClaveIdempotencia(String claveIdempotencia);

    @Query("""
            select d from DespachoInventario d
            join fetch d.formula f
            join fetch f.medicamento
            join fetch d.usuario
            where f.cita.id = :citaId and d.cantidadDespachada > 0
            order by d.fecha, d.id
            """)
    List<DespachoInventario> findDespachosFacturablesPorCita(@Param("citaId") Long citaId);
}
