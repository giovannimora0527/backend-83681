package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicoRepository extends JpaRepository<Medico, Long> {

    Optional<Medico> findByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);

    Optional<Medico> findByRegistroProfesional(String registroProfesional);

    boolean existsByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);

    boolean existsByTipoDocumentoAndNumeroDocumentoAndIdNot(String tipoDocumento, String numeroDocumento, Long id);

    boolean existsByRegistroProfesional(String registroProfesional);

    boolean existsByRegistroProfesionalAndIdNot(String registroProfesional, Long id);

    boolean existsByEspecializacion_Id(Long especializacionId);
}
