package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de la entidad {@link Cita}. Extiende
 * {@link JpaSpecificationExecutor} para soportar los filtros dinámicos y la
 * paginación del listado.
 */
public interface CitaRepository extends JpaRepository<Cita, Long>, JpaSpecificationExecutor<Cita> {

    boolean existsByMedico_Id(Long medicoId);

    boolean existsByMascota_MascotaId(Long mascotaId);

    /**
     * Busca las citas no canceladas de un veterinario que se cruzan con el
     * intervalo indicado (límites excluidos).
     *
     * @param medicoId    veterinario a revisar.
     * @param desde       inicio del intervalo.
     * @param hasta       fin del intervalo.
     * @param citaIdExcluir cita que se está editando (se excluye); null al crear.
     * @param cancelada   estado que no bloquea la agenda (CANCELADA).
     * @return citas que generan cruce de horario.
     */
    @Query("SELECT c FROM Cita c WHERE c.medico.id = :medicoId AND c.estado <> :cancelada "
            + "AND c.fechaCita > :desde AND c.fechaCita < :hasta "
            + "AND (:citaIdExcluir IS NULL OR c.citaId <> :citaIdExcluir)")
    List<Cita> buscarCrucesMedico(@Param("medicoId") Long medicoId,
                                  @Param("desde") LocalDateTime desde,
                                  @Param("hasta") LocalDateTime hasta,
                                  @Param("citaIdExcluir") Long citaIdExcluir,
                                  @Param("cancelada") EstadoCita cancelada);

    /**
     * Busca las citas no canceladas de una mascota que se cruzan con el
     * intervalo indicado (límites excluidos).
     *
     * @param mascotaId   mascota a revisar.
     * @param desde       inicio del intervalo.
     * @param hasta       fin del intervalo.
     * @param citaIdExcluir cita que se está editando (se excluye); null al crear.
     * @param cancelada   estado que no bloquea la agenda (CANCELADA).
     * @return citas que generan cruce de horario.
     */
    @Query("SELECT c FROM Cita c WHERE c.mascota.mascotaId = :mascotaId AND c.estado <> :cancelada "
            + "AND c.fechaCita > :desde AND c.fechaCita < :hasta "
            + "AND (:citaIdExcluir IS NULL OR c.citaId <> :citaIdExcluir)")
    List<Cita> buscarCrucesMascota(@Param("mascotaId") Long mascotaId,
                                   @Param("desde") LocalDateTime desde,
                                   @Param("hasta") LocalDateTime hasta,
                                   @Param("citaIdExcluir") Long citaIdExcluir,
                                   @Param("cancelada") EstadoCita cancelada);

    /**
     * Lista las citas atendidas de una mascota que todavía no tienen historia
     * clínica. Se usa en el combo dependiente del formulario de historias.
     *
     * @param mascotaId mascota a consultar.
     * @param atendida  estado ATENDIDA.
     * @return citas disponibles para generar historia, de la más reciente a la más antigua.
     */
    @Query("SELECT c FROM Cita c WHERE c.mascota.mascotaId = :mascotaId AND c.estado = :atendida "
            + "AND NOT EXISTS (SELECT h FROM HistoriaMedica h WHERE h.cita = c) ORDER BY c.fechaCita DESC")
    List<Cita> buscarAtendidasSinHistoria(@Param("mascotaId") Long mascotaId,
                                          @Param("atendida") EstadoCita atendida);
}
