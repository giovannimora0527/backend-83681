package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Repositorio de la entidad {@link HistoriaMedica}.
 */
public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long>,
        JpaSpecificationExecutor<HistoriaMedica> {

    /**
     * Indica si una cita ya tiene historia clínica registrada.
     *
     * @param citaId cita a revisar.
     * @return true si ya existe una historia para la cita.
     */
    boolean existsByCitaCitaId(Long citaId);

    /**
     * Lista las historias clínicas de una mascota, de la más reciente a la más
     * antigua. Se usa en el combo dependiente del formulario de fórmulas.
     *
     * @param mascotaId mascota a consultar.
     * @return historias de la mascota.
     */
    List<HistoriaMedica> findByMascotaMascotaIdOrderByFechaConsultaDesc(Long mascotaId);
}
