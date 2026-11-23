package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ClienteApi;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteRq;
import com.uniminuto.clinica.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @Override
    public ResponseEntity<Cliente> guardarCliente(ClienteRq request) throws BadRequestException {
        return ResponseEntity.status(201).body(clienteService.guardarCliente(request));
    }

    @Override
    public ResponseEntity<Cliente> actualizarCliente(ClienteRq request) throws BadRequestException {
        return ResponseEntity.ok(clienteService.actualizarCliente(request));
    }
}
