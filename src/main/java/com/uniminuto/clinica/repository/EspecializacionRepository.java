package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Especializacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Consulta y guarda el catálogo de especialidades veterinarias. */
@Repository
public interface EspecializacionRepository extends JpaRepository<Especializacion,Long> {

    /** Busca el registro por el nombre registrado. */
    Optional<Especializacion> findByNombre(String nombre);

    /** Busca una especialidad por su código. */
    Optional<Especializacion> findByCodigoEspecializacion(String codigoEspecializacion);

    /** Comprueba si el código de la especialidad ya está registrado. */
    Boolean existsByCodigoEspecializacion(String codigoEspecializacion);

    /** Comprueba si el nombre ya está registrado. */
    Boolean existsByNombre(String nombre);
}
