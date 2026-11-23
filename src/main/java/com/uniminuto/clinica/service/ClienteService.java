package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteRq;
<<<<<<< HEAD
=======
import com.uniminuto.clinica.models.MiRespuestaRS;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661

import java.util.List;

/**
 * Operaciones de negocio sobre clientes.
 */
public interface ClienteService {

<<<<<<< HEAD
    List<Cliente> listarClientes() throws BadRequestException;

    Cliente buscarClientesPorDocumento(String documento) throws BadRequestException;
    Cliente guardarCliente(ClienteRq request) throws BadRequestException;
    Cliente actualizarCliente(ClienteRq request) throws BadRequestException;
=======
    List<Cliente> listarClientes();

    Cliente buscarClientePorDocumento(String numeroDocumento) throws BadRequestException;

    Cliente consultarCliente(Long id) throws BadRequestException;

    MiRespuestaRS guardarCliente(ClienteRq clienteRq) throws BadRequestException;

    MiRespuestaRS actualizarCliente(ClienteRq clienteRq) throws BadRequestException;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
