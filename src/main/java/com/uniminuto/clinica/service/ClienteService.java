package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cliente;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface ClienteService {

    List<Cliente> getAllClientes();

    Cliente getClienteByNumeroDocumento(String numeroDocumento)
            throws BadRequestException;
}
