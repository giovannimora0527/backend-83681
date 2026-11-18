package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.LogCargueMasivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogCargueMasivoRepository extends JpaRepository<LogCargueMasivo, Long> {

    List<LogCargueMasivo> findAllByOrderByFechaCargueDesc();
}