package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

public interface ClienteService {

    List<Cliente> getAllClientes();

    Cliente getClienteByNumeroDocumento(String numeroDocumento)
            throws BadRequestException;

    /**
     * Consulta un cliente por su identificador.
     *
     * @param id identificador del cliente.
     * @return el cliente encontrado (404 si no existe).
     */
    Cliente getClienteById(Long id);

    MiRespuestaRS guardarCliente(ClienteRq clienteRq) throws BadRequestException;

    MiRespuestaRS actualizarCliente(ClienteRq clienteRq) throws BadRequestException;
}
