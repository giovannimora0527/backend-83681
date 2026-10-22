package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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
}
