package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.service.ClienteService;
import com.uniminuto.clinica.util.ValidadorEntidades;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Validaciones comunes (existencia de cliente, mascota, etc.).
     */
    @Autowired
    private ValidadorEntidades validador;

    @Override
    public List<Cliente> getAllClientes() {
        return this.clienteRepository.findAll();
    }

    @Override
    public Cliente consultarCliente(Long clienteId) throws BadRequestException {
        return this.validador.obtenerCliente(clienteId);
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
    public MiRespuestaRS guardarCliente(ClienteRq clienteRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(clienteRq);

        // Paso 2. Validar que no exista otro cliente con el mismo numero de documento
        Optional<Cliente> optCliente = this.clienteRepository
                .findByNumeroDocumento(clienteRq.getNumeroDocumento().trim());
        if (optCliente.isPresent()) {
            throw new BadRequestException("Ya existe un cliente con el número de documento "
                    + clienteRq.getNumeroDocumento());
        }

        // Paso 3. Crear la entidad cliente a guardar.
        Cliente cliente = new Cliente();
        this.asignarDatos(cliente, clienteRq);
        cliente.setActivo(clienteRq.getActivo() == null || clienteRq.getActivo());

        this.clienteRepository.save(cliente);

        // Paso 4. Retornar la respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Cliente guardado correctamente");

        return respuesta;
    }

    @Override
    public MiRespuestaRS actualizarCliente(ClienteRq clienteRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(clienteRq);

        if (clienteRq.getClienteId() == null) {
            throw new BadRequestException("El ID del cliente no puede estar vacío");
        }

        // Paso 2. Validar si el cliente existe en la base de datos
        Optional<Cliente> optCliente = this.clienteRepository.findById(clienteRq.getClienteId());
        if (optCliente.isEmpty()) {
            throw new BadRequestException("El cliente con ID " + clienteRq.getClienteId()
                    + " no existe en la base de datos");
        }

        // Paso 3. Validar que el numero de documento no pertenezca a otro cliente
        Optional<Cliente> optDocumento = this.clienteRepository
                .findByNumeroDocumento(clienteRq.getNumeroDocumento().trim());
        if (optDocumento.isPresent() && !optDocumento.get().getId().equals(clienteRq.getClienteId())) {
            throw new BadRequestException("Ya existe otro cliente con el número de documento "
                    + clienteRq.getNumeroDocumento());
        }

        Cliente cliente = optCliente.get();
        this.asignarDatos(cliente, clienteRq);
        if (clienteRq.getActivo() != null) {
            cliente.setActivo(clienteRq.getActivo());
        }

        this.clienteRepository.save(cliente);

        // Paso 4. Retornar la respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Cliente actualizado correctamente");

        return respuesta;
    }

    private void asignarDatos(Cliente cliente, ClienteRq clienteRq) {
        cliente.setTipoDocumento(clienteRq.getTipoDocumento());
        cliente.setNumeroDocumento(clienteRq.getNumeroDocumento().trim());
        cliente.setNombres(clienteRq.getNombres().trim());
        cliente.setApellidos(clienteRq.getApellidos().trim());
        cliente.setFechaNacimiento(clienteRq.getFechaNacimiento());
        cliente.setGenero(clienteRq.getGenero());
        cliente.setTelefono(clienteRq.getTelefono());
        cliente.setDireccion(clienteRq.getDireccion());
    }

    private void validarObjetoEntrada(ClienteRq clienteRq) throws BadRequestException {
        if (clienteRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }

        if (clienteRq.getTipoDocumento() == null || clienteRq.getTipoDocumento().trim().isEmpty()) {
            throw new BadRequestException("El tipo de documento no puede estar vacío");
        }

        if (clienteRq.getNumeroDocumento() == null || clienteRq.getNumeroDocumento().trim().isEmpty()) {
            throw new BadRequestException("El número de documento no puede estar vacío");
        }

        if (clienteRq.getNombres() == null || clienteRq.getNombres().trim().isEmpty()) {
            throw new BadRequestException("Los nombres del cliente no pueden estar vacíos");
        }

        if (clienteRq.getApellidos() == null || clienteRq.getApellidos().trim().isEmpty()) {
            throw new BadRequestException("Los apellidos del cliente no pueden estar vacíos");
        }
    }
}
