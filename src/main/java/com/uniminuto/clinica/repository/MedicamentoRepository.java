package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio del catálogo de medicamentos.
 */
@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    /**
     * Busca un medicamento por nombre exacto.
     *
     * @param nombre nombre a buscar.
     * @return medicamento encontrado, si existe.
     */
    Optional<Medicamento> findByNombre(String nombre);

    /**
     * Lista los medicamentos ordenados por nombre.
     *
     * @return lista ordenada.
     */
    List<Medicamento> findAllByOrderByNombreAsc();
}
