package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ClienteApi;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Expone las operaciones HTTP relacionadas con un propietario de mascotas. */
@RestController
public class ClienteApiController implements ClienteApi {

    @Autowired
    private ClienteService clienteService;

    /** Consulta los propietarios. */
    @Override
    public ResponseEntity<List<Cliente>> listarClientes() throws BadRequestException {
        return ResponseEntity.ok(clienteService.listarClientes());
    }

    /** Busca los propietarios cuyo documento coincide con el criterio. */
    @Override
    public ResponseEntity<Cliente> buscarClientesPorDocumento(String documento) throws BadRequestException {
        return ResponseEntity.ok(clienteService.buscarClientesPorDocumento(documento));
    }

    /** Registra el propietario. */
    @Override
    public ResponseEntity<MiRespuestaRS> crearCliente(Cliente cliente) {
        return ResponseEntity.ok(clienteService.crearCliente(cliente));
    }

    /** Actualiza el propietario. */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarCliente(Cliente cliente) {
        return ResponseEntity.ok(clienteService.actualizarCliente(cliente));
    }
}
