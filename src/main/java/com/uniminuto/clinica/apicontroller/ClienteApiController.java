package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ClienteApi;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
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
    }
}
