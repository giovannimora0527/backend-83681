package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Especializacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Consulta y guarda el catálogo de especialidades veterinarias. */
@Repository
public interface EspecializacionRepository extends JpaRepository<Especializacion,Long> {

    Optional<Especializacion> findByNombre(String nombre);

    Optional<Especializacion> findByCodigoEspecializacion(String codigoEspecializacion);

    Boolean existsByCodigoEspecializacion(String codigoEspecializacion);

    Boolean existsByNombre(String nombre);
}
