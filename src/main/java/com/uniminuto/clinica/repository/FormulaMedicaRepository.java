package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;

@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    List<FormulaMedica> findAllByOrderByFechaCreacionRegistroAsc();

    List<FormulaMedica> findByCita_IdInOrderByFechaCreacionRegistroDesc(List<Long> citaIds);

    List<FormulaMedica> findAllByOrderByFechaCreacionRegistroDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select f from FormulaMedica f where f.id = :id")
    Optional<FormulaMedica> findLockedById(Long id);

    /** Busca las fórmulas vigentes que aún tienen cantidades por dispensar. */
    @org.springframework.data.jpa.repository.Query(value = "SELECT f.id FROM formula_medica f "
            + "JOIN cita c ON c.id = f.cita_id "
            + "JOIN cliente cl ON cl.cliente_id = c.cliente_id "
            + "JOIN mascota m ON m.mascota_id = c.mascota_id "
            + "WHERE f.activo = 1 AND f.estado = 'VIGENTE' "
            + "AND (:fechaGeneracion IS NULL OR DATE(f.fecha_emision) = :fechaGeneracion) "
            + "AND (:filtro IS NULL OR TRIM(:filtro) = '' "
            + "OR LOWER(f.numero_formula) LIKE CONCAT('%', LOWER(:filtro), '%') "
            + "OR CAST(f.id AS CHAR) LIKE CONCAT('%', :filtro, '%') "
            + "OR LOWER(CONCAT(cl.nombres, ' ', cl.apellidos)) LIKE CONCAT('%', LOWER(:filtro), '%') "
            + "OR LOWER(m.nombre_mascota) LIKE CONCAT('%', LOWER(:filtro), '%')) "
            + "AND EXISTS (SELECT 1 FROM formula_detalle fd "
            + "WHERE fd.formula_id = f.id AND fd.cantidad_prescrita > COALESCE(( "
            + "SELECT SUM(dd.cantidad_entregada) FROM despacho_detalle dd "
            + "JOIN despacho d ON d.id = dd.despacho_id "
            + "WHERE dd.formula_detalle_id = fd.id AND d.estado <> 'CANCELADO'), 0)) "
            + "ORDER BY f.fecha_emision DESC, f.id DESC",
            nativeQuery = true)
    List<Long> buscarFormulasPendientesDespacho(
            @org.springframework.data.repository.query.Param("filtro") String filtro,
            @org.springframework.data.repository.query.Param("fechaGeneracion") LocalDate fechaGeneracion);
}
