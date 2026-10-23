package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Metodo para buscar un cliente por su numero de documento.
     * @param numeroDocumento documento a buscar.
     * @return posible cliente encontrado.
     */
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);

    boolean existsByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);

    boolean existsByTipoDocumentoAndNumeroDocumentoAndIdNot(
            String tipoDocumento,
            String numeroDocumento,
            Long id);
}
