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

    /** Consulta registros asociados a la cita indicada. */
    Optional<HistoriaMedica> findByCita(Cita cita);

    /** Consulta las historias de la mascota en orden de creación, de la más reciente a la más antigua. */
    List<HistoriaMedica> findByMascota_MascotaIdOrderByFechaCreacionDescIdDesc(Long mascotaId);

    /** Comprueba si existe un registro asociado a la cita. */
    boolean existsByCita(Cita cita);

    /** Comprueba si otra fórmula ya está asociada a la cita. */
    boolean existsByCitaAndIdNot(Cita cita, Long id);

}
