package com.uniminuto.clinica.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cliente")
public interface ClienteApi {

    @GetMapping(value = "/all", produces = {"application/json"})
    ResponseEntity<List<Cliente>> getClientes() throws BadRequestException;

    @GetMapping(value = "/cliente-documento", produces = {"application/json"})
    ResponseEntity<Cliente> getClientesByNumeroDocumento(
            @RequestParam String numeroDocumento
    ) throws BadRequestException;

    @PostMapping(value = "/guardar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarCliente(@RequestBody ClienteRq clienteRq) throws BadRequestException;

    @PostMapping(value = "/actualizar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarCliente(@RequestBody ClienteRq clienteRq) throws BadRequestException;
}