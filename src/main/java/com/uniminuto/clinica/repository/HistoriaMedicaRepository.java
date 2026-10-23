package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a datos de las historias clínicas.
 */
@Repository
public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long> {

    /**
     * Lista las historias de la consulta más reciente a la más antigua.
     *
     * @return historias ordenadas por fecha de consulta descendente.
     */
    List<HistoriaMedica> findAllByOrderByFechaConsultaDesc();

    /**
     * Lista las historias de una mascota de la consulta más reciente a la más antigua.
     *
     * @param mascotaId identificador de la mascota.
     * @return historias de la mascota.
     */
    List<HistoriaMedica> findByMascota_MascotaIdOrderByFechaConsultaDesc(Long mascotaId);

    /**
     * Indica si una cita ya generó una historia clínica.
     *
     * @param citaId identificador de la cita.
     * @return true si ya existe una historia para esa cita.
     */
    boolean existsByCita_Id(Long citaId);
}
