package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Consulta las notas vinculadas a las historias clínicas del paciente. */
@Repository
public interface AnotacionHistoriaRepository extends JpaRepository<AnotacionHistoria, Long> {
    List<AnotacionHistoria> findByHistoria(HistoriaMedica historia);

    List<AnotacionHistoria> findByHistoria_IdInOrderByFechaAscIdAsc(List<Long> historiaIds);
}
