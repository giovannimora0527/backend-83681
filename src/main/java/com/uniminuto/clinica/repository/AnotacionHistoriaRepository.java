package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Consulta las notas vinculadas a las historias clínicas del paciente. */
@Repository
public interface AnotacionHistoriaRepository extends JpaRepository<AnotacionHistoria, Long> {
    /** Consulta las anotaciones de la historia indicada. */
    List<AnotacionHistoria> findByHistoria(HistoriaMedica historia);

    /** Consulta las anotaciones de varias historias en orden de fecha e identificador. */
    List<AnotacionHistoria> findByHistoria_IdInOrderByFechaAscIdAsc(List<Long> historiaIds);
}
