package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Consulta y guarda los datos relacionados con una opción de navegación. */
@Repository
public interface MenuRepository extends JpaRepository<Menu, Integer> {
}
