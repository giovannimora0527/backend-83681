package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    @Query("SELECT m FROM Medicamento m WHERE m.cantidadActual <= m.stockMinimo")
    List<Medicamento> findProductosConStockBajo();

    @Query("SELECT m FROM Medicamento m WHERE m.cantidadActual = 0")
    List<Medicamento> findProductosAgotados();

    @Query("SELECT m FROM Medicamento m WHERE m.fechaVence BETWEEN :fechaInicio AND :fechaFin")
    List<Medicamento> findProductosProximosAVencer(@Param("fechaInicio") LocalDate fechaInicio,
                                                   @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT m FROM Medicamento m WHERE m.fechaVence <= :fechaActual")
    List<Medicamento> findProductosConVencimientoAntesDe(@Param("fechaActual") LocalDate fechaActual);

    List<Medicamento> findByCantidadActualGreaterThan(Integer cantidadActual);

    List<Medicamento> findByCantidadActualLessThanEqualStockMinimo();

    List<Medicamento> findByCantidadActualEquals(Integer cantidadActual);

    List<Medicamento> findByFechaVenceBetween(LocalDate fechaInicio, LocalDate fechaFin);

    boolean existsByNombre(String nombre);

    boolean existsByCodigoAlfanumerico(String codigoAlfanumerico);
}
