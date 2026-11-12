package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

public interface ClienteService {

    List<Cliente> listarClientes() throws BadRequestException;

    Cliente buscarClientesPorDocumento(String documento) throws BadRequestException;

    MiRespuestaRS crearCliente(Cliente cliente);

    MiRespuestaRS actualizarCliente(Cliente cliente);
}
