package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteRq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RequestMapping("/cliente")
public interface ClienteApi {

    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Cliente>> listarClientes()
            throws BadRequestException;


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
}
