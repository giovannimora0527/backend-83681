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
import java.util.Optional;

/**
 * Consulta la agenda y el historial de citas. Protege la cita mientras se actualiza su
 * programación o su atención.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /** Obtiene el registro por su identificador y lo bloquea durante la operación. */
    @Query(value = "SELECT id FROM cita WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<Long> bloquearPorId(Long id);

    /** Lee el estado de la cita con bloqueo durante la operación. */
    @Query(value = "SELECT estado FROM cita WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<String> bloquearEstado(Long id);

    /** Consulta las citas agendadas dentro del intervalo indicado. */
    List<Cita> findByFechaHoraBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /** Busca la historia clínica asociada a la mascota y al veterinario. */
    Optional<Cita> findByMascotaAndMedico(Mascota mascota, Medico medico);

    /** Consulta las citas del veterinario en el intervalo indicado. */
    List<Cita> findByMedicoAndFechaHoraBetween(Medico medico, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /** Consulta las citas de la mascota, empezando por la más reciente. */
    List<Cita> findByMascotaOrderByFechaHoraDesc(Mascota mascota);

    /** Consulta las citas del veterinario en orden de fecha y hora. */
    List<Cita> findByMedicoIdOrderByFechaHoraAsc(Long medicoId);

    /** Consulta las citas dentro del intervalo indicado. */
    @Query(value = """
            SELECT c.* FROM cita c
            WHERE (:medicoId IS NULL OR c.medico_id = :medicoId)
              AND c.fecha_hora < :fin
              AND COALESCE(c.hora_fin, DATE_ADD(c.fecha_hora, INTERVAL c.duracion_minutos MINUTE)) > :inicio
            ORDER BY c.fecha_hora, c.id
            """, nativeQuery = true)
    List<Cita> listarIntervalo(@Param("medicoId") Long medicoId, @Param("inicio") LocalDateTime inicio,
                               @Param("fin") LocalDateTime fin);

}
