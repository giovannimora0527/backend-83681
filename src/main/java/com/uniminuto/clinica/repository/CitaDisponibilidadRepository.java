package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.entity.CitaDisponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CitaDisponibilidadRepository extends JpaRepository<CitaDisponibilidad, Long> {
    List<CitaDisponibilidad> findByMedicoIdAndDiaSemanaAndActivoTrue(Long medicoId, Integer diaSemana);
    List<CitaDisponibilidad> findByMedicoIdAndActivoTrueOrderByDiaSemanaAscHoraInicioAsc(Long medicoId);
}
