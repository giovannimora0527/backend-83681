package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.CitaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio (acceso a base de datos) de las citas.
 */
@Repository
public interface CitaRepository extends JpaRepository<CitaMedica, Long> {

    /**
     * Lista todas las citas, de la mas reciente a la mas antigua.
     *
     * @return citas ordenadas por fecha y hora descendente.
     */
    List<CitaMedica> findAllByOrderByFechaHoraDesc();

    /**
     * Lista las citas entre dos fechas, de la mas reciente a la mas antigua.
     *
     * @param fechaInicio fecha inicial del rango.
     * @param fechaFinal fecha final del rango.
     * @return citas encontradas.
     */
    List<CitaMedica> findAllByFechaHoraBetweenOrderByFechaHoraDesc(
            LocalDateTime fechaInicio, LocalDateTime fechaFinal);

    /**
     * Lista las citas de una mascota, de la mas reciente a la mas antigua.
     *
     * @param mascotaId id de la mascota.
     * @return citas de la mascota.
     */
    List<CitaMedica> findByMascotaMascotaIdOrderByFechaHoraDesc(Long mascotaId);

    /**
     * Busca citas activas (no canceladas) de un veterinario que se crucen con
     * el rango indicado. Sirve para la regla "un veterinario no puede tener
     * dos citas con menos de 15 minutos de diferencia".
     *
     * @param medicoId id del veterinario.
     * @param desde inicio del rango (fecha de la cita - 15 minutos).
     * @param hasta fin del rango (fecha de la cita + 15 minutos).
     * @param citaExcluidaId id de la cita que se esta editando (para no chocar
     *                       consigo misma). Null cuando es una cita nueva.
     * @return citas que generan conflicto.
     */
    @Query("SELECT c FROM CitaMedica c WHERE c.medico.id = :medicoId "
            + "AND c.estado <> 'CANCELADA' "
            + "AND c.fechaHora > :desde AND c.fechaHora < :hasta "
            + "AND (:citaExcluidaId IS NULL OR c.id <> :citaExcluidaId)")
    List<CitaMedica> buscarCruceVeterinario(@Param("medicoId") Long medicoId,
                                            @Param("desde") LocalDateTime desde,
                                            @Param("hasta") LocalDateTime hasta,
                                            @Param("citaExcluidaId") Long citaExcluidaId);

    /**
     * Busca citas activas (no canceladas) de una mascota que se crucen con
     * el rango indicado. Sirve para la regla "una mascota no puede tener
     * dos citas simultaneas".
     *
     * @param mascotaId id de la mascota.
     * @param desde inicio del rango.
     * @param hasta fin del rango.
     * @param citaExcluidaId id de la cita que se esta editando, o null.
     * @return citas que generan conflicto.
     */
    @Query("SELECT c FROM CitaMedica c WHERE c.mascota.mascotaId = :mascotaId "
            + "AND c.estado <> 'CANCELADA' "
            + "AND c.fechaHora > :desde AND c.fechaHora < :hasta "
            + "AND (:citaExcluidaId IS NULL OR c.id <> :citaExcluidaId)")
    List<CitaMedica> buscarCruceMascota(@Param("mascotaId") Long mascotaId,
                                        @Param("desde") LocalDateTime desde,
                                        @Param("hasta") LocalDateTime hasta,
                                        @Param("citaExcluidaId") Long citaExcluidaId);
}
