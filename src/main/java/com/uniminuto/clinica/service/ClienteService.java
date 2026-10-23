package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

public interface ClienteService {

    List<Cliente> getAllClientes();

    /**
     * Consulta un cliente por su id.
     *
     * @param clienteId id del cliente.
     * @return cliente encontrado.
     * @throws BadRequestException si el id es invalido o el cliente no existe (404).
     */
    Cliente consultarCliente(Long clienteId) throws BadRequestException;

    Cliente getClienteByNumeroDocumento(String numeroDocumento)
            throws BadRequestException;

    MiRespuestaRS guardarCliente(ClienteRq clienteRq) throws BadRequestException;

    MiRespuestaRS actualizarCliente(ClienteRq clienteRq) throws BadRequestException;
}
