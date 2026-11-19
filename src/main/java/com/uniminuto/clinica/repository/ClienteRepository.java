package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Consulta y guarda los datos relacionados con un propietario de mascotas. */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** Busca el registro por su número de documento. */
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);

    /** Comprueba si ya existe un registro con el documento indicado. */
    boolean existsByNumeroDocumento(String numeroDocumento);

    /** Comprueba si otro registro usa el mismo número de documento. */
    boolean existsByNumeroDocumentoAndIdNot(String numeroDocumento, Long id);

    /** Busca el registro asociado a la cuenta indicada. */
    Optional<Cliente> findByUsuarioId(Long usuarioId);
}
