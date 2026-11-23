package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.models.ClienteRq;
import com.uniminuto.clinica.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;


    @Override
    public List<Cliente> listarClientes() throws BadRequestException {
        return clienteRepository.findAll().stream()
                .sorted((c1, c2) -> c1.getNombres().compareToIgnoreCase(c2.getNombres()))
                .toList();
    }

    @Override
    public Cliente buscarClientesPorDocumento(String documento) throws BadRequestException {
        Optional<Cliente> clienteOptional = clienteRepository.findByNumeroDocumento(documento);
        if (clienteOptional.isPresent()) {
            return clienteOptional.get();
        } else {
            throw new BadRequestException("Cliente no encontrado con el documento: " + documento);
        }
    }

    @Override
    @Transactional
    public Cliente guardarCliente(ClienteRq request) {
        validar(request);
        String documento = request.getNumeroDocumento().trim();
        if (clienteRepository.existsByNumeroDocumentoIgnoreCase(documento)) {
            throw new BadRequestException("Ya existe un cliente con ese documento.");
        }
        Cliente cliente = new Cliente();
        aplicar(cliente, request);
        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public Cliente actualizarCliente(ClienteRq request) {
        validar(request);
        if (request.getId() == null) throw new BadRequestException("El identificador del cliente es obligatorio.");
        Cliente cliente = clienteRepository.findById(request.getId())
                .orElseThrow(() -> new BadRequestException("El cliente solicitado no existe."));
        String documento = request.getNumeroDocumento().trim();
        if (clienteRepository.existsByNumeroDocumentoIgnoreCaseAndIdNot(documento, cliente.getId())) {
            throw new BadRequestException("Ya existe un cliente con ese documento.");
        }
        aplicar(cliente, request);
        return clienteRepository.save(cliente);
    }

    private void validar(ClienteRq r) {
        if (r == null || texto(r.getTipoDocumento()) == null || texto(r.getNumeroDocumento()) == null
                || texto(r.getNombres()) == null || texto(r.getApellidos()) == null || r.getFechaNacimiento() == null) {
            throw new BadRequestException("Tipo y número de documento, nombres, apellidos y fecha de nacimiento son obligatorios.");
        }
        if (r.getNumeroDocumento().trim().length() > 20 || r.getTipoDocumento().trim().length() > 10
                || r.getNombres().trim().length() > 100 || r.getApellidos().trim().length() > 100) {
            throw new BadRequestException("Uno o más campos exceden la longitud permitida.");
        }
        if (r.getTelefono() != null && r.getTelefono().trim().length() > 20) {
            throw new BadRequestException("El teléfono no puede exceder 20 caracteres.");
        }
        if (r.getFechaNacimiento().isAfter(LocalDate.now())) throw new BadRequestException("La fecha de nacimiento no puede ser futura.");
        if (r.getGenero() != null && !r.getGenero().isBlank() && !List.of("F", "M", "O").contains(r.getGenero().trim().toUpperCase())) {
            throw new BadRequestException("El género debe ser F, M u O.");
        }
    }

    private void aplicar(Cliente c, ClienteRq r) {
        c.setTipoDocumento(r.getTipoDocumento().trim().toUpperCase());
        c.setNumeroDocumento(r.getNumeroDocumento().trim());
        c.setNombres(r.getNombres().trim()); c.setApellidos(r.getApellidos().trim());
        c.setFechaNacimiento(r.getFechaNacimiento());
        c.setGenero(texto(r.getGenero()) == null ? null : r.getGenero().trim().toUpperCase());
        c.setTelefono(texto(r.getTelefono())); c.setDireccion(texto(r.getDireccion()));
        c.setActivo(r.getActivo() == null ? true : r.getActivo());
    }

    private String texto(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
