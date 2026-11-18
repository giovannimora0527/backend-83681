package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Consulta la historia de una cita y los antecedentes de la mascota. */
@Repository
public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long> {

    Optional<HistoriaMedica> findByCita(Cita cita);

    List<HistoriaMedica> findByMascota_MascotaIdOrderByFechaCreacionDescIdDesc(Long mascotaId);

    boolean existsByCita(Cita cita);

    boolean existsByCitaAndIdNot(Cita cita, Long id);

}
