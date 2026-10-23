package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.CitaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<CitaMedica, Long> {

    List<CitaMedica> findAllByFechaHoraBetweenOrderByFechaHoraDesc(
            LocalDateTime fechaInicio, LocalDateTime fechaFinal);

    List<CitaMedica> findByMascotaAndMedicoAndFechaHoraBetweenOrderByFechaHoraDesc(
            Mascota mascota, Medico medico,
            LocalDateTime fechaInicio, LocalDateTime fechaFinal);


    List<CitaMedica> findByMedicoAndFechaHoraBetweenOrderByFechaHoraDesc(
            Medico medico,
            LocalDateTime fechaInicio, LocalDateTime fechaFinal);
}
