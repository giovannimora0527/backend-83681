package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Consulta y guarda los datos relacionados con un perfil de usuario. */
@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Integer> {
}
