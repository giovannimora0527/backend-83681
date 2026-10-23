package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repositorio de la entidad {@link AnotacionHistoria}. Los listados se
 * resuelven con filtros dinámicos ({@link JpaSpecificationExecutor}).
 */
public interface AnotacionHistoriaRepository extends JpaRepository<AnotacionHistoria, Long>,
        JpaSpecificationExecutor<AnotacionHistoria> {
}
