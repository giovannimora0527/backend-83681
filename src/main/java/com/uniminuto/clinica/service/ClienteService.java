package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/** Define las operaciones disponibles para gestionar un propietario de mascotas. */
public interface ClienteService {

    /** Consulta los propietarios. */
    List<Cliente> listarClientes() throws BadRequestException;

    /** Busca los propietarios cuyo documento coincide con el criterio. */
    Cliente buscarClientesPorDocumento(String documento) throws BadRequestException;

    /** Registra el propietario. */
    MiRespuestaRS crearCliente(Cliente cliente);

    /** Actualiza el propietario. */
    MiRespuestaRS actualizarCliente(Cliente cliente);
}
