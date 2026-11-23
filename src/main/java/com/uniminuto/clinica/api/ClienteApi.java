package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteRq;
<<<<<<< HEAD
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RequestMapping("/cliente")
public interface ClienteApi {

=======
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrato REST del recurso cliente.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cliente")
public interface ClienteApi {

    /**
     * Lista todos los clientes ordenados por apellidos y nombres.
     *
     * @return lista de clientes.
     * @throws BadRequestException si ocurre un error de negocio.
     */
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Cliente>> listarClientes()
            throws BadRequestException;

<<<<<<< HEAD

    @GetMapping(value = "/buscar-por-documento",
            produces = {"application/json"})
    ResponseEntity<Cliente> buscarClientesPorDocumento(
            @RequestParam String documento
    )
            throws BadRequestException;

    @PostMapping(value = "/guardar", produces = "application/json", consumes = "application/json")
    ResponseEntity<Cliente> guardarCliente(@RequestBody ClienteRq request) throws BadRequestException;

    @PostMapping(value = "/actualizar", produces = "application/json", consumes = "application/json")
    ResponseEntity<Cliente> actualizarCliente(@RequestBody ClienteRq request) throws BadRequestException;
=======
    /**
     * Busca un cliente por su número de documento.
     *
     * @param numeroDocumento documento a buscar.
     * @return cliente encontrado.
     * @throws BadRequestException si no existe.
     */
    @GetMapping(value = "/buscar",
            produces = {"application/json"})
    ResponseEntity<Cliente> buscarClientePorDocumento(
            @RequestParam String numeroDocumento)
            throws BadRequestException;

    /**
     * Consulta un cliente por su identificador.
     *
     * @param id identificador del cliente.
     * @return cliente encontrado.
     * @throws BadRequestException si no existe.
     */
    @GetMapping(value = "/consultar",
            produces = {"application/json"})
    ResponseEntity<Cliente> consultarCliente(
            @RequestParam Long id)
            throws BadRequestException;

    /**
     * Registra un nuevo cliente.
     *
     * @param clienteRq datos del cliente.
     * @return resultado de la operación.
     * @throws BadRequestException si los datos no son válidos.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarCliente(
            @RequestBody ClienteRq clienteRq)
            throws BadRequestException;

    /**
     * Actualiza un cliente existente.
     *
     * @param clienteRq datos actualizados del cliente.
     * @return resultado de la operación.
     * @throws BadRequestException si el cliente no existe o los datos no son válidos.
     */
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarCliente(
            @RequestBody ClienteRq clienteRq)
            throws BadRequestException;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
