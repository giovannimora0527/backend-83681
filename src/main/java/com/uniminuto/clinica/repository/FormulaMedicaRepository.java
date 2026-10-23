package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio de la entidad {@link FormulaMedica}.
 */
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long>,
        JpaSpecificationExecutor<FormulaMedica> {

    /**
     * Indica si ya existe una fórmula activa con el mismo medicamento en la
     * misma historia clínica (para evitar fórmulas duplicadas).
     *
     * @param historiaId       historia clínica.
     * @param medicamento      nombre del medicamento (sin distinguir mayúsculas).
     * @param formulaIdExcluir fórmula que se está editando; null al crear.
     * @return true si ya existe una fórmula igual.
     */
    @Query("SELECT COUNT(f) > 0 FROM FormulaMedica f WHERE f.historiaMedica.historiaId = :historiaId "
            + "AND LOWER(f.medicamento) = LOWER(:medicamento) AND f.activo = true "
            + "AND (:formulaIdExcluir IS NULL OR f.formulaId <> :formulaIdExcluir)")
    boolean existeFormulaActiva(@Param("historiaId") Long historiaId,
                                @Param("medicamento") String medicamento,
                                @Param("formulaIdExcluir") Long formulaIdExcluir);
}
