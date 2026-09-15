package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Busca citas dentro del rango [fechaInicial, fechaFinal] y las ordena
     * desde la más reciente hasta la más antigua.
     */
    List<Cita> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime fechaInicial, LocalDateTime fechaFinal);

    /**
     * Retorna todas las citas ordenadas desde la más reciente hasta la más antigua.
     */
    List<Cita> findAllByOrderByFechaHoraDesc();
}
