// repository/HistoriaMedicaRepository.java
package com.uniminuto.clinica.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uniminuto.clinica.entity.HistoriaMedica;

public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long> {
}