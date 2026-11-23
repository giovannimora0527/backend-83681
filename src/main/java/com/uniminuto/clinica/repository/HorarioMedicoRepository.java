package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.HorarioMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HorarioMedicoRepository extends JpaRepository<HorarioMedico, Long> {
    List<HorarioMedico> findByMedico_IdAndActivoTrueOrderByDiaSemanaAscHoraInicioAsc(Long medicoId);
}
