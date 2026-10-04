package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
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
    public MiRespuestaRS guardarCliente(ClienteRq rq) throws BadRequestException {
        this.validar(rq);

        Cliente cliente = new Cliente();
        cliente.setTipoDocumento(rq.getTipoDocumento());
        cliente.setNumeroDocumento(rq.getNumeroDocumento());
        cliente.setNombres(rq.getNombres());
        cliente.setApellidos(rq.getApellidos());
        cliente.setFechaNacimiento(rq.getFechaNacimiento());
        cliente.setGenero(rq.getGenero());
        cliente.setTelefono(rq.getTelefono());
        cliente.setDireccion(rq.getDireccion());
        cliente.setActivo(rq.getActivo() != null ? rq.getActivo() : true);

        clienteRepository.save(cliente);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Cliente guardado correctamente");
        return respuesta;
    }

    @Override
    public MiRespuestaRS actualizarCliente(ClienteRq rq) throws BadRequestException {
        this.validar(rq);
        if (rq.getId() == null) {
            throw new BadRequestException("El ID del cliente es obligatorio para actualizar");
        }

        Optional<Cliente> optCliente = clienteRepository.findById(rq.getId());
        if (optCliente.isEmpty()) {
            throw new BadRequestException("El cliente con ID " + rq.getId() + " no existe");
        }

        Cliente cliente = optCliente.get();
        cliente.setTipoDocumento(rq.getTipoDocumento());
        cliente.setNumeroDocumento(rq.getNumeroDocumento());
        cliente.setNombres(rq.getNombres());
        cliente.setApellidos(rq.getApellidos());
        cliente.setFechaNacimiento(rq.getFechaNacimiento());
        cliente.setGenero(rq.getGenero());
        cliente.setTelefono(rq.getTelefono());
        cliente.setDireccion(rq.getDireccion());
        cliente.setActivo(rq.getActivo());

        clienteRepository.save(cliente);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Cliente actualizado correctamente");
        return respuesta;
    }

    private void validar(ClienteRq rq) throws BadRequestException {
        if (rq == null || rq.getNumeroDocumento() == null || rq.getNumeroDocumento().trim().isEmpty()) {
            throw new BadRequestException("El número de documento es obligatorio");
        }
        if (rq.getNombres() == null || rq.getNombres().trim().isEmpty()) {
            throw new BadRequestException("Los nombres son obligatorios");
        }
    }
}
