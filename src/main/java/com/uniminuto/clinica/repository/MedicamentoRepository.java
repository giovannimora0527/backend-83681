package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;

/** Consultas persistentes para medicamentos y productos médicos. */
@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    /** Lista productos ordenados por nombre. */
    List<Medicamento> findAllByOrderByNombreAsc();

    /** Lista productos con existencias disponibles. */
    List<Medicamento> findByCantidadActualGreaterThanOrderByNombreAsc(Integer cantidad);

    /** Lista productos agotados. */
    List<Medicamento> findByCantidadActualLessThanEqualOrderByNombreAsc(Integer cantidad);

    /** Lista productos con existencias iguales o inferiores al mínimo. */
    @Query("select m from Medicamento m where m.cantidadActual <= m.stockMinimo order by m.nombre asc")
    List<Medicamento> buscarMedicamentosConStockBajo();

    /** Comprueba si el nombre ya está registrado. */
    boolean existsByNombreIgnoreCase(String nombre);

    /** Comprueba si otro registro utiliza el mismo nombre. */
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    /** Comprueba si el código ya está registrado. */
    boolean existsByCodigoIgnoreCase(String codigo);

    /** Comprueba si otro registro utiliza el código. */
    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    /** Busca un producto por su código. */
    Optional<Medicamento> findByCodigoIgnoreCase(String codigo);

    /** Busca un producto por su nombre. */
    Optional<Medicamento> findByNombreIgnoreCase(String nombre);

    /** Bloquea el producto durante una operación que modifica existencias. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Medicamento m where m.id = :id")
    Optional<Medicamento> findLockedById(@Param("id") Long id);
}
