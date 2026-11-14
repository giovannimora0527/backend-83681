package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormulaDetalleRepository extends JpaRepository<FormulaDetalle, Long> {

    List<FormulaDetalle> findByFormulaIdOrderByIdAsc(Long formulaId);

    @Query(value = "SELECT id FROM formula_medica "
            + "WHERE id = :formulaId AND activo = 1 AND estado = 'VIGENTE' FOR UPDATE",
            nativeQuery = true)
    Optional<Long> buscarFormulaVigenteParaBloqueo(@Param("formulaId") Long formulaId);

    @Query(value = "SELECT cita_id FROM formula_medica WHERE id = :formulaId", nativeQuery = true)
    Optional<Long> buscarCitaIdPorFormula(@Param("formulaId") Long formulaId);
}
