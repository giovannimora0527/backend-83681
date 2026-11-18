package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.LoteMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoteMedicamentoRepository extends JpaRepository<LoteMedicamento, Long> {

    List<LoteMedicamento> findByMedicamentoId(Long medicamentoId);
}
