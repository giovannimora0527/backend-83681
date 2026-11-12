package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long> {

    Optional<HistoriaMedica> findByCita(Cita cita);

    boolean existsByCita(Cita cita);

    boolean existsByCitaAndIdNot(Cita cita, Long id);

}
