package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.HorarioDisponibilidad;
import com.uniminuto.clinica.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HorarioDisponibilidadRepository extends JpaRepository<HorarioDisponibilidad, Long> {
    List<HorarioDisponibilidad> findByMedicoAndDiaSemana(Medico medico, Integer diaSemana);
}
