package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByFechaHoraBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    Optional<Cita> findByMascotaAndMedico(Mascota mascota, Medico medico);

    List<Cita> findByMedicoAndFechaHoraBetween(Medico medico, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<Cita> findByMascotaOrderByFechaHoraDesc(Mascota mascota);
    List<Cita> findByMedicoIdAndFechaHoraBetweenOrderByFechaHoraAsc(Long medicoId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    @Query("select count(c) > 0 from Cita c where c.medico.id = :medicoId and upper(c.estado) <> 'CANCELADA' " +
            "and c.fechaHora < :fin and c.fechaFin > :inicio and (:excluirId is null or c.id <> :excluirId)")
    boolean existeCruceMedico(@Param("medicoId") Long medicoId, @Param("inicio") LocalDateTime inicio,
                              @Param("fin") LocalDateTime fin, @Param("excluirId") Long excluirId);

    @Query("select count(c) > 0 from Cita c where c.mascota.mascotaId = :mascotaId and upper(c.estado) <> 'CANCELADA' " +
            "and c.fechaHora < :fin and c.fechaFin > :inicio and (:excluirId is null or c.id <> :excluirId)")
    boolean existeCruceMascota(@Param("mascotaId") Integer mascotaId, @Param("inicio") LocalDateTime inicio,
                               @Param("fin") LocalDateTime fin, @Param("excluirId") Long excluirId);

}
