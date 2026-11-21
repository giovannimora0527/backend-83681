package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ClienteApi;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.uniminuto.clinica.repository.ClienteRepository;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClienteApiController implements ClienteApi {

    @Autowired
    private ClienteService clienteService;

    @Override
    public ResponseEntity<List<Cliente>> listarClientes() throws BadRequestException {
        return ResponseEntity.ok(clienteService.listarClientes());
    }

    @Override
    public ResponseEntity<Cliente> buscarClientesPorDocumento(String documento) throws BadRequestException {
        return ResponseEntity.ok(clienteService.buscarClientesPorDocumento(documento));
    }
    @Autowired
    private ClienteRepository clienteRepository;

    public record NuevoCliente(String nombres, String apellidos, String tipoDocumento,
            String numeroDocumento, LocalDate fechaNacimiento, String genero,
            String telefono, String direccion) {}

    @PostMapping("/cliente/crear")
    public ResponseEntity<Cliente> crearCliente(@RequestBody NuevoCliente datos) {
        if (datos == null || vacio(datos.nombres()) || vacio(datos.apellidos()) ||
                vacio(datos.tipoDocumento()) || vacio(datos.numeroDocumento()) || datos.fechaNacimiento() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Complete los campos obligatorios");
        if (datos.fechaNacimiento().isAfter(LocalDate.now()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha de nacimiento invalida");
        if (clienteRepository.findByNumeroDocumento(datos.numeroDocumento().trim()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El documento ya esta registrado");
        Cliente c = new Cliente();
        c.setNombres(datos.nombres().trim()); c.setApellidos(datos.apellidos().trim());
        c.setTipoDocumento(datos.tipoDocumento().trim()); c.setNumeroDocumento(datos.numeroDocumento().trim());
        c.setFechaNacimiento(datos.fechaNacimiento());
        c.setGenero(vacio(datos.genero()) ? null : datos.genero().trim().toUpperCase());
        c.setTelefono(datos.telefono()); c.setDireccion(datos.direccion()); c.setActivo(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteRepository.save(c));
    }
    private static boolean vacio(String v) { return v == null || v.isBlank(); }
}
