package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de la entidad {@link Cita}.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Busca las citas cuya fecha esté entre una fecha inicial y una fecha
     * final, ordenadas de la más reciente a la más antigua.
     *
     * @param fechaInicial fecha inicial del rango de búsqueda.
     * @param fechaFinal   fecha final del rango de búsqueda.
     * @return lista de citas encontradas en el rango, ordenadas descendentemente.
     */
    List<Cita> findByFechaCitaBetweenOrderByFechaCitaDesc(LocalDateTime fechaInicial, LocalDateTime fechaFinal);
}
