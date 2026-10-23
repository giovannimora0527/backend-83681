package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    boolean existsByNombreIgnoreCaseAndPresentacionIgnoreCase(String nombre, String presentacion);

    boolean existsByNombreIgnoreCaseAndPresentacionIgnoreCaseAndIdNot(
            String nombre,
            String presentacion,
            Long id);
}
