package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ClienteApi;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.service.MascotaService;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClienteApiController implements ClienteApi {

    private final ClienteService clienteService;

    /**
     * Servicio de mascotas, usado para listar las mascotas del cliente.
     */
    private final MascotaService mascotaService;

    public ClienteApiController(ClienteService clienteService, MascotaService mascotaService) {
        this.clienteService = clienteService;
        this.mascotaService = mascotaService;
    }

    @Override
    public ResponseEntity<List<Cliente>> getClientes() throws BadRequestException {
        return ResponseEntity.ok(this.clienteService.getAllClientes());
    }

    @Override
    public ResponseEntity<Cliente> getClientesByNumeroDocumento(String numeroDocumento) throws BadRequestException {
        return ResponseEntity.ok(this.clienteService.getClienteByNumeroDocumento(numeroDocumento));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Cliente> consultarCliente(Long clienteId) {
        return ResponseEntity.ok(this.clienteService.consultarCliente(clienteId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<Mascota>> listarMascotasCliente(Long clienteId) {
        return ResponseEntity.ok(this.mascotaService.listarMascotasPorCliente(clienteId));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarCliente(ClienteRq rq) {
        return ResponseEntity.status(201).body(clienteService.crearCliente(rq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarCliente(ClienteRq rq) {
        return ResponseEntity.ok(clienteService.actualizarCliente(rq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> eliminarCliente(Long id) {
        return ResponseEntity.ok(clienteService.eliminarCliente(id));
    }
}
