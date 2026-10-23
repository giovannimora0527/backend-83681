package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Acceso a datos de las citas médicas.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Lista todas las citas de la más reciente a la más antigua.
     *
     * @return citas ordenadas por fecha descendente.
     */
    List<Cita> findAllByOrderByFechaDesc();

    /**
     * Lista las citas de una mascota de la más reciente a la más antigua.
     *
     * @param mascotaId identificador de la mascota.
     * @return citas de la mascota.
     */
    List<Cita> findByMascota_MascotaIdOrderByFechaDesc(Long mascotaId);

    /**
     * Busca las citas de un veterinario dentro de un rango de fechas, excluyendo un estado.
     * Se usa para validar su disponibilidad.
     *
     * @param medicoId identificador del veterinario.
     * @param desde inicio del rango (incluido).
     * @param hasta fin del rango (incluido).
     * @param estado estado a excluir (CANCELADA).
     * @return citas que ocupan el rango.
     */
    List<Cita> findByMedico_IdAndFechaBetweenAndEstadoNot(Long medicoId, LocalDateTime desde, LocalDateTime hasta, String estado);

    /**
     * Busca las citas de una mascota dentro de un rango de fechas, excluyendo un estado.
     * Se usa para evitar citas simultáneas de una misma mascota.
     *
     * @param mascotaId identificador de la mascota.
     * @param desde inicio del rango (incluido).
     * @param hasta fin del rango (incluido).
     * @param estado estado a excluir (CANCELADA).
     * @return citas que ocupan el rango.
     */
    List<Cita> findByMascota_MascotaIdAndFechaBetweenAndEstadoNot(Long mascotaId, LocalDateTime desde, LocalDateTime hasta, String estado);
}
