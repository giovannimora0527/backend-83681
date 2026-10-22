package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de médicos.
 */
@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    /**
     * Busca un médico por tipo y número de documento.
     *
     * @param tipoDocumento   tipo de documento.
     * @param numeroDocumento número de documento.
     * @return médico encontrado, si existe.
     */
    Optional<Medico> findByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);

    /**
     * Busca un médico por registro profesional.
     *
     * @param registroProfesional registro a buscar.
     * @return médico encontrado, si existe.
     */
    Optional<Medico> findByRegistroProfesional(String registroProfesional);

    /**
     * Lista los médicos ordenados por apellidos y nombres.
     *
     * @return lista ordenada.
     */
    List<Medico> findAllByOrderByApellidosAscNombresAsc();
}
