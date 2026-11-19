package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Consulta el directorio veterinario y permite proteger la asignación de un médico durante la
 * programación de una cita.
 */
@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    /** Consulta los veterinarios disponibles para la fecha indicada. */
    @Query(value = """
            SELECT m.* FROM medico m
            WHERE m.estado = 1
              AND EXISTS (
                SELECT 1 FROM horario_disponibilidad h
                WHERE h.medico_id = m.id AND h.activo = 1
                  AND h.dia_semana = WEEKDAY(:fechaInicio) + 1
                  AND h.hora_inicio <= TIME(:fechaInicio) AND h.hora_fin >= TIME(:fechaFin)
              )
              AND NOT EXISTS (
                SELECT 1 FROM cita c
                WHERE (c.medico_id = m.id OR c.mascota_id = :mascotaId)
                  AND (:citaId IS NULL OR c.id <> :citaId)
                  AND c.estado <> 'CANCELADA'
                  AND c.fecha_hora < :fechaFin
                  AND COALESCE(c.hora_fin, DATE_ADD(c.fecha_hora, INTERVAL c.duracion_minutos MINUTE)) > :fechaInicio
              )
            ORDER BY m.nombres, m.apellidos, m.id
            """, nativeQuery = true)
    List<Medico> findDisponiblesPorFecha(@Param("fechaInicio") LocalDateTime fechaInicio,
                                        @Param("fechaFin") LocalDateTime fechaFin,
                                        @Param("citaId") Long citaId, @Param("mascotaId") Long mascotaId);

    /** Comprueba si ya existe un registro con el documento indicado. */
    boolean existsByNumeroDocumento(String numeroDocumento);

    /** Comprueba si el registro profesional ya está asociado a un veterinario. */
    boolean existsByRegistroProfesional(String registroProfesional);

    /** Comprueba si otro registro usa el mismo número de documento. */
    boolean existsByNumeroDocumentoAndIdNot(String numeroDocumento, Long id);

    /** Comprueba si otro veterinario usa el mismo registro profesional. */
    boolean existsByRegistroProfesionalAndIdNot(String registroProfesional, Long id);
}
