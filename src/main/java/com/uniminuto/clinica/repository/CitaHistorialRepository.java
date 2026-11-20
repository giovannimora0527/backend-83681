package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.entity.CitaHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CitaHistorialRepository extends JpaRepository<CitaHistorial, Long> {
    List<CitaHistorial> findByCitaIdOrderByCreadoEnAsc(Long citaId);
}
