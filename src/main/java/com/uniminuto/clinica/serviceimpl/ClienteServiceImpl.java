package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.service.ClienteService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    public List<Cliente> getAllClientes() {
        return this.clienteRepository.findAll();
    }

    @Override
    public Cliente getClienteByNumeroDocumento(String numeroDocumento)
            throws BadRequestException {
        if (numeroDocumento == null || numeroDocumento.isEmpty()) {
            throw new BadRequestException("El número de documento no puede ser nulo o vacío");
        }

        Optional<Cliente> optCliente = this.clienteRepository.findByNumeroDocumento(numeroDocumento);
        if (optCliente.isEmpty()) {
            throw new BadRequestException("Cliente no encontrado");
        }

        return optCliente.get();
    }
}
