package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/**
 * Operaciones de negocio sobre clientes.
 */
public interface ClienteService {

    List<Cliente> listarClientes();

    Cliente buscarClientePorDocumento(String numeroDocumento) throws BadRequestException;

    Cliente consultarCliente(Long id) throws BadRequestException;

    MiRespuestaRS guardarCliente(ClienteRq clienteRq) throws BadRequestException;

    MiRespuestaRS actualizarCliente(ClienteRq clienteRq) throws BadRequestException;
}
