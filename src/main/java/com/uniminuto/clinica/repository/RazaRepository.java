package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Raza;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RazaRepository extends JpaRepository<Raza, Integer> {

    boolean existsByNombreIgnoreCaseAndEspecieIgnoreCase(String nombre, String especie);

    boolean existsByNombreIgnoreCaseAndEspecieIgnoreCaseAndRazaIdNot(
            String nombre,
            String especie,
            Integer razaId);
}
