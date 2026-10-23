package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cliente")
public interface ClienteApi {

    /**
     * Consulta un cliente por su id.
     *
     * @param clienteId id del cliente.
     * @return cliente encontrado (404 si no existe).
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/consultar", produces = {"application/json"})
    ResponseEntity<Cliente> consultarCliente(@RequestParam Long clienteId)
            throws BadRequestException;

    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/all",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Cliente>> getClientes()
            throws BadRequestException;


    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/cliente-documento",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<Cliente> getClientesByNumeroDocumento(
            @RequestParam String numeroDocumento
    )
            throws BadRequestException;

    /**
     * Metodo para guardar un cliente nuevo.
     *
     * @param clienteRq datos del cliente a guardar.
     * @return Respuesta del servicio.
     * @throws BadRequestException excepcion.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarCliente(
            @RequestBody ClienteRq clienteRq
    )
            throws BadRequestException;

    /**
     * Metodo para actualizar un cliente existente.
     *
     * @param clienteRq datos del cliente a actualizar.
     * @return Respuesta del servicio.
     * @throws BadRequestException excepcion.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarCliente(
            @RequestBody ClienteRq clienteRq
    )
            throws BadRequestException;
}
