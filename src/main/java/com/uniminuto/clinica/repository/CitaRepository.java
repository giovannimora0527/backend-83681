package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad Cita.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Filtra las citas cuya fecha se encuentre dentro del rango recibido y las
     * ordena de la mas reciente a la mas antigua.
     *
     * @param fechaInicial Limite inferior del rango de busqueda.
     * @param fechaFinal Limite superior del rango de busqueda.
     * @return Lista de citas del rango ordenadas descendentemente.
     */
    List<Cita> findByFechaCitaBetweenOrderByFechaCitaDesc(
            LocalDateTime fechaInicial, LocalDateTime fechaFinal);
}
