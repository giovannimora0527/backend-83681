package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

/** Implementa las operaciones y validaciones relacionadas con un propietario de mascotas. */
@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /** Consulta los propietarios. */
    @Override
    public List<Cliente> listarClientes() throws BadRequestException {
        return clienteRepository.findAll().stream()
                .sorted((c1, c2) -> c1.getNombres().compareToIgnoreCase(c2.getNombres()))
                .toList();
    }

    /** Busca los propietarios cuyo documento coincide con el criterio. */
    @Override
    public Cliente buscarClientesPorDocumento(String documento) throws BadRequestException {
        Optional<Cliente> clienteOptional = clienteRepository.findByNumeroDocumento(documento);
        if (clienteOptional.isPresent()) {
            return clienteOptional.get();
        } else {
            throw new BadRequestException("Cliente no encontrado con el documento: " + documento);
        }
    }

    /** Registra el propietario. */
    @Override
    public MiRespuestaRS crearCliente(Cliente cliente) {
        validarCliente(cliente);
        validarUsuario(cliente.getUsuarioId(), null);
        String documento = cliente.getNumeroDocumento().trim();
        if (clienteRepository.existsByNumeroDocumento(documento)) {
            throw new BadRequestException("Ya existe un cliente con ese número de documento.");
        }
        cliente.setId(null);
        cliente.setTipoDocumento(cliente.getTipoDocumento().trim());
        cliente.setNumeroDocumento(documento);
        cliente.setNombres(cliente.getNombres().trim());
        cliente.setApellidos(cliente.getApellidos().trim());
        cliente.setActivo(cliente.getActivo() == null ? Boolean.TRUE : cliente.getActivo());
        clienteRepository.save(cliente);
        return respuesta("Cliente creado correctamente.");
    }

    /** Actualiza el propietario. */
    @Override
    public MiRespuestaRS actualizarCliente(Cliente cliente) {
        validarCliente(cliente);
        if (cliente.getId() == null || cliente.getId() <= 0) {
            throw new BadRequestException("El id del cliente es obligatorio para actualizar.");
        }
        Cliente existente = clienteRepository.findById(cliente.getId())
                .orElseThrow(() -> new BadRequestException("El cliente no existe."));
        String documento = cliente.getNumeroDocumento().trim();
        if (clienteRepository.existsByNumeroDocumentoAndIdNot(documento, cliente.getId())) {
            throw new BadRequestException("Ya existe otro cliente con ese número de documento.");
        }
        existente.setTipoDocumento(cliente.getTipoDocumento().trim());
        existente.setNumeroDocumento(documento);
        existente.setNombres(cliente.getNombres().trim());
        existente.setApellidos(cliente.getApellidos().trim());
        existente.setFechaNacimiento(cliente.getFechaNacimiento());
        existente.setGenero(limpiar(cliente.getGenero()));
        existente.setTelefono(limpiar(cliente.getTelefono()));
        existente.setDireccion(limpiar(cliente.getDireccion()));
        if (cliente.getUsuarioId() != null) {
            validarUsuario(cliente.getUsuarioId(), existente.getId());
            existente.setUsuarioId(cliente.getUsuarioId());
        }
        if (cliente.getActivo() != null) existente.setActivo(cliente.getActivo());
        clienteRepository.save(existente);
        return respuesta("Cliente actualizado correctamente.");
    }

    /** Valida el usuario. */
    private void validarUsuario(Long usuarioId, Long clienteId) {
        if (usuarioId == null) {
            return;
        }
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new BadRequestException("El usuario asociado al cliente no existe.");
        }
        boolean vinculadoAOtroCliente = clienteRepository.findByUsuarioId(usuarioId)
                .filter(cliente -> !cliente.getId().equals(clienteId))
                .isPresent();
        if (vinculadoAOtroCliente) {
            throw new BadRequestException("El usuario ya está asociado a otro cliente.");
        }
    }

    /** Valida el propietario. */
    private void validarCliente(Cliente cliente) {
        if (cliente == null) throw new BadRequestException("La información del cliente es obligatoria.");
        if (vacio(cliente.getTipoDocumento()) || vacio(cliente.getNumeroDocumento())
                || vacio(cliente.getNombres()) || vacio(cliente.getApellidos())) {
            throw new BadRequestException("Tipo y número de documento, nombres y apellidos son obligatorios.");
        }
        if (cliente.getFechaNacimiento() == null || cliente.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new BadRequestException("La fecha de nacimiento es obligatoria y no puede estar en el futuro.");
        }
        if (cliente.getGenero() != null && !cliente.getGenero().isBlank()
                && !List.of("M", "F").contains(cliente.getGenero().trim().toUpperCase())) {
            throw new BadRequestException("El género debe ser M o F.");
        }
    }

    /** Comprueba si el texto está ausente o solo contiene espacios. */
    private boolean vacio(String valor) { return valor == null || valor.isBlank(); }
    /** Elimina espacios sobrantes al inicio y al final del valor. */
    private String limpiar(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
    /** Prepara el mensaje que devuelve la operación. */
    private MiRespuestaRS respuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }
}
