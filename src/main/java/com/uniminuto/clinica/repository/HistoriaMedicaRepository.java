package com.uniminuto.clinica.repository;

<<<<<<< HEAD
import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long> {

    Optional<HistoriaMedica> findByMascota(Mascota mascota);

    boolean existsByMascota(Mascota mascota);

    boolean existsByMascotaAndIdNot(Mascota mascota, Long id);
=======
import com.uniminuto.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de historias clínicas.
 */
@Repository
public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long>, JpaSpecificationExecutor<HistoriaMedica> {

    /**
     * Lista todas las historias, de la consulta más reciente a la más antigua.
     *
     * @return lista ordenada por fecha y hora descendente.
     */
    List<HistoriaMedica> findAllByOrderByFechaHoraDesc();

    /**
     * Lista las historias de un rango de fechas de consulta.
     *
     * @param inicio inicio del rango (inclusive).
     * @param fin    fin del rango (inclusive).
     * @return historias del rango ordenadas de la más reciente a la más antigua.
     */
    List<HistoriaMedica> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Lista las historias de una mascota.
     *
     * @param mascotaId identificador de la mascota.
     * @return historias de la mascota ordenadas de la más reciente a la más antigua.
     */
    List<HistoriaMedica> findByMascotaMascotaIdOrderByFechaHoraDesc(Integer mascotaId);

    /**
     * Busca la historia que se originó en una cita.
     *
     * @param citaId identificador de la cita.
     * @return historia asociada, si existe.
     */
    Optional<HistoriaMedica> findByCitaId(Long citaId);
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
