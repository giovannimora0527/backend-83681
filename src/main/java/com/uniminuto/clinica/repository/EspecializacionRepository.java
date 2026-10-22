package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Especializacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio del catálogo de especializaciones.
 */
@Repository
public interface EspecializacionRepository extends JpaRepository<Especializacion, Long> {

    /**
     * Busca una especialización por su código.
     *
     * @param codigoEspecializacion código a buscar.
     * @return especialización encontrada, si existe.
     */
    Optional<Especializacion> findByCodigoEspecializacion(String codigoEspecializacion);

    /**
     * Busca una especialización por nombre exacto.
     *
     * @param nombre nombre a buscar.
     * @return especialización encontrada, si existe.
     */
    Optional<Especializacion> findByNombre(String nombre);

    /**
     * Lista las especializaciones ordenadas por nombre.
     *
     * @return lista ordenada.
     */
    List<Especializacion> findAllByOrderByNombreAsc();
}
