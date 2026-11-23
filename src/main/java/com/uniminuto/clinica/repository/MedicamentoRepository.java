package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< HEAD
import java.util.Optional;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    Optional<Medicamento> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
=======
import java.util.List;
import java.util.Optional;

/**
 * Repositorio del catálogo de medicamentos.
 */
@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    /**
     * Busca un medicamento por nombre exacto.
     *
     * @param nombre nombre a buscar.
     * @return medicamento encontrado, si existe.
     */
    Optional<Medicamento> findByNombre(String nombre);

    /**
     * Lista los medicamentos ordenados por nombre.
     *
     * @return lista ordenada.
     */
    List<Medicamento> findAllByOrderByNombreAsc();
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
