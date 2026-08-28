package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Metodo para buscar un cliente por su numero de documento.
     * @param numeroDocumento documento a buscar.
     * @return posible cliente encontrado.
     */
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);
    
}
