package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    @Query("SELECT c FROM Cita c WHERE c.medico = :medico AND c.fechaHoraInicio >= :inicio AND c.fechaHoraInicio < :fin ORDER BY c.fechaHoraInicio")
    List<Cita> findByMedicoAndRange(@Param("medico") Medico medico,
                                    @Param("inicio") LocalDateTime inicio,
                                    @Param("fin") LocalDateTime fin);

    @Query("SELECT c FROM Cita c WHERE c.medico = :medico AND c.estado <> com.uniminuto.clinica.entity.EstadoCita.CANCELADA")
    List<Cita> findActiveByMedico(@Param("medico") Medico medico);

    @Query("SELECT c FROM Cita c WHERE c.mascota = :mascota AND c.estado <> com.uniminuto.clinica.entity.EstadoCita.CANCELADA")
    List<Cita> findActiveByMascota(@Param("mascota") Mascota mascota);

    List<Cita> findByMascotaOrderByFechaHoraInicioDesc(Mascota mascota);
}
