package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Despacho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/** Consultas y bloqueos de los encabezados de despacho. */
@Repository
public interface DespachoRepository extends JpaRepository<Despacho, Long> {

    /** Obtiene el identificador de la fórmula ligada al despacho. */
    @Query("select d.formulaId from Despacho d where d.id = :id")
    Optional<Long> buscarFormulaId(@Param("id") Long id);

    /** Consulta los registros del identificador más alto al más bajo. */
    List<Despacho> findAllByOrderByIdDesc();

    /** Consulta los medicamentos de la fórmula desde el identificador más alto. */
    List<Despacho> findByFormulaIdOrderByIdDesc(Long formulaId);

    /** Busca el proceso activo más reciente de una fórmula. */
    Optional<Despacho> findFirstByFormulaIdAndEstadoInOrderByIdDesc(Long formulaId, List<String> estados);

    /** Obtiene el registro por identificador con bloqueo durante la operación. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Despacho d where d.id = :id")
    Optional<Despacho> findLockedById(@Param("id") Long id);
}
