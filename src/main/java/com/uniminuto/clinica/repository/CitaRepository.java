package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cita c where c.id = :id")
    Optional<Cita> findByIdForUpdate(@Param("id") Long id);

    List<Cita> findByFechaHoraBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    Optional<Cita> findByMascotaAndMedico(Mascota mascota, Medico medico);

    List<Cita> findByMedicoAndFechaHoraBetween(Medico medico, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<Cita> findByMascotaOrderByFechaHoraDesc(Mascota mascota);

    @Query(value = "SELECT * FROM cita c WHERE c.medico_id = :medicoId "
            + "AND c.estado NOT IN ('CANCELADA', 'ANULADA') AND (:citaId IS NULL OR c.id <> :citaId) "
            + "AND c.fecha_hora < :fin AND DATE_ADD(c.fecha_hora, INTERVAL c.duracion_minutos MINUTE) > :inicio",
            nativeQuery = true)
    List<Cita> buscarCrucesMedico(@Param("medicoId") Long medicoId,
            @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin,
            @Param("citaId") Long citaId);

    @Query(value = "SELECT * FROM cita c WHERE c.mascota_id = :mascotaId "
            + "AND c.estado NOT IN ('CANCELADA', 'ANULADA') AND (:citaId IS NULL OR c.id <> :citaId) "
            + "AND c.fecha_hora < :fin AND DATE_ADD(c.fecha_hora, INTERVAL c.duracion_minutos MINUTE) > :inicio",
            nativeQuery = true)
    List<Cita> buscarCrucesMascota(@Param("mascotaId") Integer mascotaId,
            @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin,
            @Param("citaId") Long citaId);

}
