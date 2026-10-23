package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.ClienteRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final MascotaRepository mascotaRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository, MascotaRepository mascotaRepository) {
        this.clienteRepository = clienteRepository;
        this.mascotaRepository = mascotaRepository;
    }

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

    /**
     * {@inheritDoc}
     */
    @Override
    public Cliente consultarCliente(Long clienteId) {
        return this.clienteRepository.findById(clienteId)
                .orElseThrow(() -> new NotFoundException("El cliente con id " + clienteId + " no existe."));
    }

    @Override
    @Transactional
    public MiRespuestaRS crearCliente(ClienteRq rq) {
        validarCliente(rq);
        if (clienteRepository.existsByTipoDocumentoAndNumeroDocumento(
                rq.getTipoDocumento(), rq.getNumeroDocumento())) {
            throw new ConflictException("Ya existe un cliente con ese tipo y número de documento.");
        }

        Cliente cliente = mapearCliente(rq, new Cliente());
        cliente = clienteRepository.save(cliente);
        return new MiRespuestaRS(HttpStatus.CREATED.value(), "Cliente creado correctamente.", cliente.getId());
    }

    @Override
    @Transactional
    public MiRespuestaRS actualizarCliente(ClienteRq rq) {
        validarCliente(rq);
        if (rq.getId() == null) {
            throw new BadRequestException("El identificador del cliente es obligatorio.");
        }
        Cliente cliente = consultarCliente(rq.getId());
        if (clienteRepository.existsByTipoDocumentoAndNumeroDocumentoAndIdNot(
                rq.getTipoDocumento(), rq.getNumeroDocumento(), rq.getId())) {
            throw new ConflictException("Ya existe otro cliente con ese tipo y número de documento.");
        }

        mapearCliente(rq, cliente);
        clienteRepository.save(cliente);
        return new MiRespuestaRS(HttpStatus.OK.value(), "Cliente actualizado correctamente.", cliente.getId());
    }

    @Override
    @Transactional
    public MiRespuestaRS eliminarCliente(Long id) {
        Cliente cliente = consultarCliente(id);
        if (mascotaRepository.existsByCliente_Id(id)) {
            throw new ConflictException("No se puede eliminar el cliente porque tiene mascotas asociadas.");
        }
        clienteRepository.delete(cliente);
        return new MiRespuestaRS(HttpStatus.OK.value(), "Cliente eliminado correctamente.", id);
    }

    private void validarCliente(ClienteRq rq) {
        if (rq == null || esVacio(rq.getTipoDocumento()) || esVacio(rq.getNumeroDocumento())
                || esVacio(rq.getNombres()) || esVacio(rq.getApellidos())) {
            throw new BadRequestException("El tipo y número de documento, nombres y apellidos son obligatorios.");
        }
    }

    private Cliente mapearCliente(ClienteRq rq, Cliente cliente) {
        cliente.setTipoDocumento(rq.getTipoDocumento().trim());
        cliente.setNumeroDocumento(rq.getNumeroDocumento().trim());
        cliente.setNombres(rq.getNombres().trim());
        cliente.setApellidos(rq.getApellidos().trim());
        cliente.setFechaNacimiento(rq.getFechaNacimiento());
        cliente.setGenero(normalizarOpcional(rq.getGenero()));
        cliente.setTelefono(normalizarOpcional(rq.getTelefono()));
        cliente.setDireccion(normalizarOpcional(rq.getDireccion()));
        cliente.setActivo(rq.getActivo() == null ? Boolean.TRUE : rq.getActivo());
        return cliente;
    }

    private String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
