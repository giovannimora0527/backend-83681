package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Raza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio del catálogo de razas.
 */
@Repository
public interface RazaRepository extends JpaRepository<Raza, Integer> {

    /**
     * Busca una raza por nombre exacto.
     *
     * @param nombre nombre de la raza.
     * @return raza encontrada, si existe.
     */
    Optional<Raza> findByNombre(String nombre);

    /**
     * Lista las razas ordenadas por nombre ascendente.
     *
     * @return lista ordenada de razas.
     */
    List<Raza> findAllByOrderByNombreAsc();
}
