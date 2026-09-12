package com.uniminuto.clinica.repository;


import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {
    FormulaMedica findAllByOrderByFechaCreacionRegistroDesc(Long idFormulaMedica);
}
