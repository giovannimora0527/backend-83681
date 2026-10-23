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
     * @param clienteId identificador del cliente.
     * @return cliente encontrado.
     * @throws com.uniminuto.clinica.exception.NotFoundException si el cliente no existe.
     */
    Cliente consultarCliente(Long clienteId);

    MiRespuestaRS crearCliente(ClienteRq rq);

    MiRespuestaRS actualizarCliente(ClienteRq rq);

    MiRespuestaRS eliminarCliente(Long id);
}
