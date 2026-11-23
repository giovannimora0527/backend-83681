package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ClienteApi;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteRq;
<<<<<<< HEAD
=======
import com.uniminuto.clinica.models.MiRespuestaRS;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
import com.uniminuto.clinica.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST del recurso cliente.
 */
@RestController
public class ClienteApiController implements ClienteApi {

    @Autowired
    private ClienteService clienteService;

    @Override
    public ResponseEntity<List<Cliente>> listarClientes() throws BadRequestException {
<<<<<<< HEAD
        return ResponseEntity.ok(clienteService.listarClientes());
    }

    @Override
    public ResponseEntity<Cliente> buscarClientesPorDocumento(String documento) throws BadRequestException {
        return ResponseEntity.ok(clienteService.buscarClientesPorDocumento(documento));
    }

    @Override
    public ResponseEntity<Cliente> guardarCliente(ClienteRq request) throws BadRequestException {
        return ResponseEntity.status(201).body(clienteService.guardarCliente(request));
    }

    @Override
    public ResponseEntity<Cliente> actualizarCliente(ClienteRq request) throws BadRequestException {
        return ResponseEntity.ok(clienteService.actualizarCliente(request));
=======
        return ResponseEntity.ok(this.clienteService.listarClientes());
    }

    @Override
    public ResponseEntity<Cliente> buscarClientePorDocumento(String numeroDocumento) throws BadRequestException {
        return ResponseEntity.ok(this.clienteService.buscarClientePorDocumento(numeroDocumento));
    }

    @Override
    public ResponseEntity<Cliente> consultarCliente(Long id) throws BadRequestException {
        return ResponseEntity.ok(this.clienteService.consultarCliente(id));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarCliente(ClienteRq clienteRq) throws BadRequestException {
        return ResponseEntity.ok(this.clienteService.guardarCliente(clienteRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarCliente(ClienteRq clienteRq) throws BadRequestException {
        return ResponseEntity.ok(this.clienteService.actualizarCliente(clienteRq));
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
    }
}
