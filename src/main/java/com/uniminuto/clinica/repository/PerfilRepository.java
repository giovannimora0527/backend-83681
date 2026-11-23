package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Integer> {

    Optional<Perfil> findByNombreIgnoreCase(String nombre);

    List<Perfil> findAllByActivoTrueOrderByNombreAsc();
}
