package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de clientes.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca un cliente por su número de documento.
     *
     * @param numeroDocumento documento a buscar.
     * @return cliente encontrado, si existe.
     */
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);

    /**
     * Lista los clientes ordenados por apellidos y nombres.
     *
     * @return lista ordenada de clientes.
     */
    List<Cliente> findAllByOrderByApellidosAscNombresAsc();
}
