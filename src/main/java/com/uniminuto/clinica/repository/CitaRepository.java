package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

<<<<<<< HEAD
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

=======
/**
 * Repositorio de citas.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long>, JpaSpecificationExecutor<Cita> {

    /**
     * Lista todas las citas, de la más reciente a la más antigua.
     *
     * @return lista ordenada por fecha y hora descendente.
     */
    List<Cita> findAllByOrderByFechaHoraDesc();

    /**
     * Lista las citas dentro de un rango de fechas, de la más reciente a la más antigua.
     *
     * @param inicio fecha y hora inicial (inclusive).
     * @param fin    fecha y hora final (inclusive).
     * @return citas del rango.
     */
    List<Cita> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Citas de un médico dentro de una ventana de tiempo. Se usa para validar choques de horario.
     *
     * @param medicoId identificador del médico.
     * @param inicio   inicio de la ventana (inclusive).
     * @param fin      fin de la ventana (inclusive).
     * @return citas del médico en la ventana.
     */
    List<Cita> findByMedicoIdAndFechaHoraBetween(Long medicoId, LocalDateTime inicio, LocalDateTime fin);

    /**
     * Citas de una mascota dentro de una ventana de tiempo. Se usa para validar choques de horario.
     *
     * @param mascotaId identificador de la mascota.
     * @param inicio    inicio de la ventana (inclusive).
     * @param fin       fin de la ventana (inclusive).
     * @return citas de la mascota en la ventana.
     */
    List<Cita> findByMascotaMascotaIdAndFechaHoraBetween(Integer mascotaId, LocalDateTime inicio, LocalDateTime fin);
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
