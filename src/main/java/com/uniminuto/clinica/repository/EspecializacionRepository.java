package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Especializacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EspecializacionRepository extends JpaRepository<Especializacion, Long> {
    Optional<Especializacion> findByCodigoEspecializacion(String codigoEspecializacion);
}
