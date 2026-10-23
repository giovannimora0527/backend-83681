package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Especializacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecializacionRepository extends JpaRepository<Especializacion, Long> {

    boolean existsByCodigoEspecializacionIgnoreCase(String codigoEspecializacion);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByCodigoEspecializacionIgnoreCaseAndIdNot(String codigoEspecializacion, Long id);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
