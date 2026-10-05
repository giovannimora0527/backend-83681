package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.service.ClienteService;
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

    @Override
    public Cliente guardarCliente(Cliente cliente) throws BadRequestException {
        validarCliente(cliente);
        if (clienteRepository.findByNumeroDocumento(cliente.getNumeroDocumento()).isPresent()) {
            throw new BadRequestException("Ya existe un cliente con ese número de documento");
        }
        cliente.setId(null);
        return clienteRepository.save(cliente);
    }

    @Override
    public Cliente actualizarCliente(Cliente cliente) throws BadRequestException {
        validarCliente(cliente);
        if (cliente.getId() == null || !clienteRepository.existsById(cliente.getId())) {
            throw new BadRequestException("El cliente que desea actualizar no existe");
        }
        if (clienteRepository.findByNumeroDocumento(cliente.getNumeroDocumento())
                .filter(existing -> !existing.getId().equals(cliente.getId())).isPresent()) {
            throw new BadRequestException("Ya existe un cliente con ese número de documento");
        }
        return clienteRepository.save(cliente);
    }

    private void validarCliente(Cliente cliente) throws BadRequestException {
        if (cliente == null
                || cliente.getTipoDocumento() == null || cliente.getTipoDocumento().isBlank()
                || cliente.getNumeroDocumento() == null || cliente.getNumeroDocumento().isBlank()
                || cliente.getNombres() == null || cliente.getNombres().isBlank()
                || cliente.getApellidos() == null || cliente.getApellidos().isBlank()) {
            throw new BadRequestException("Tipo y número de documento, nombres y apellidos son obligatorios");
        }
    }
}
