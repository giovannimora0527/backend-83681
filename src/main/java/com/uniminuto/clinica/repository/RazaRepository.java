package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Raza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

<<<<<<< HEAD
@Repository
public interface RazaRepository extends JpaRepository<Raza, Integer> {

    Optional<Raza> findByNombre(String nombre);

    Optional<Raza> findByEspecieAndNombre(String especie, String nombre);

=======
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
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
