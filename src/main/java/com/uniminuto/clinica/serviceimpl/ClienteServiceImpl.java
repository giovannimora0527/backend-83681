package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.models.ClienteRq;
import com.uniminuto.clinica.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de la lógica de negocio de clientes.
 */
@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;


    @Override
<<<<<<< HEAD
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
=======
    public List<Cliente> listarClientes() {
        return clienteRepository.findAllByOrderByApellidosAscNombresAsc();
    }

    @Override
    public Cliente buscarClientePorDocumento(String numeroDocumento) throws BadRequestException {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            throw new BadRequestException("El número de documento no puede estar vacío");
        }
        return clienteRepository.findByNumeroDocumento(numeroDocumento.trim())
                .orElseThrow(() -> new BadRequestException("Cliente no encontrado", HttpStatus.NOT_FOUND));
    }

    @Override
    public Cliente consultarCliente(Long id) throws BadRequestException {
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del cliente no puede estar vacío ni ser menor o igual a cero");
        }
        return clienteRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("El cliente con ID " + id + " no existe", HttpStatus.NOT_FOUND));
    }

    @Override
    public MiRespuestaRS guardarCliente(ClienteRq clienteRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(clienteRq);

        // Paso 2. Evito documentos duplicados
        Optional<Cliente> existente = this.clienteRepository.findByNumeroDocumento(clienteRq.getNumeroDocumento().trim());
        if (existente.isPresent()) {
            throw new BadRequestException("Ya existe un cliente con el número de documento " + clienteRq.getNumeroDocumento(), HttpStatus.CONFLICT);
        }

        // Paso 3. Creo la entidad cliente a guardar
        Cliente cliente = new Cliente();
        this.cargarDatos(cliente, clienteRq);
        cliente.setActivo(clienteRq.getActivo() != null ? clienteRq.getActivo() : Boolean.TRUE);

        this.clienteRepository.save(cliente);

        // Paso 4. Retorno la respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Cliente guardado correctamente");
        return respuesta;
    }

    @Override
    public MiRespuestaRS actualizarCliente(ClienteRq clienteRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(clienteRq);

        // Paso 2. Verifico que el cliente exista
        if (clienteRq.getClienteId() == null || clienteRq.getClienteId() <= 0) {
            throw new BadRequestException("El ID del cliente no puede estar vacío ni ser menor o igual a cero");
        }
        Cliente cliente = this.clienteRepository.findById(clienteRq.getClienteId())
                .orElseThrow(() -> new BadRequestException("El cliente con ID " + clienteRq.getClienteId() + " no existe", HttpStatus.NOT_FOUND));

        // Paso 3. El documento no puede pertenecer a otro cliente
        Optional<Cliente> conMismoDocumento = this.clienteRepository.findByNumeroDocumento(clienteRq.getNumeroDocumento().trim());
        if (conMismoDocumento.isPresent() && !conMismoDocumento.get().getClienteId().equals(cliente.getClienteId())) {
            throw new BadRequestException("Ya existe otro cliente con el número de documento " + clienteRq.getNumeroDocumento(), HttpStatus.CONFLICT);
        }

        // Paso 4. Actualizo los datos
        this.cargarDatos(cliente, clienteRq);
        if (clienteRq.getActivo() != null) {
            cliente.setActivo(clienteRq.getActivo());
        }
        this.clienteRepository.save(cliente);

        // Paso 5. Retorno la respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Cliente actualizado correctamente");
        return respuesta;
    }

    private void cargarDatos(Cliente cliente, ClienteRq clienteRq) {
        cliente.setTipoDocumento(clienteRq.getTipoDocumento().trim());
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
        if (clienteRq.getTipoDocumento() == null || clienteRq.getTipoDocumento().isBlank()) {
            throw new BadRequestException("El tipo de documento del cliente es obligatorio");
        }
        if (clienteRq.getNumeroDocumento() == null || clienteRq.getNumeroDocumento().isBlank()) {
            throw new BadRequestException("El número de documento del cliente es obligatorio");
        }
        if (clienteRq.getNombres() == null || clienteRq.getNombres().isBlank()) {
            throw new BadRequestException("Los nombres del cliente son obligatorios");
        }
        if (clienteRq.getApellidos() == null || clienteRq.getApellidos().isBlank()) {
            throw new BadRequestException("Los apellidos del cliente son obligatorios");
        }
        if (clienteRq.getFechaNacimiento() == null || clienteRq.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new BadRequestException("La fecha de nacimiento es obligatoria y no puede ser futura");
        }
        if (clienteRq.getGenero() != null && !clienteRq.getGenero().matches("[MF]")) {
            throw new BadRequestException("El género debe ser M o F");
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
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
