package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Propietarios", description = "Administración y Recepción pueden gestionar propietarios. Veterinaria tiene acceso de consulta.")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cliente")
public interface ClienteApi {

    @Operation(summary = "Listar propietarios")
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Cliente>> listarClientes()
            throws BadRequestException;


    @Operation(summary = "Buscar propietarios por documento")
    @GetMapping(value = "/buscar-por-documento",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<Cliente> buscarClientesPorDocumento(
            @RequestParam String documento
    )
            throws BadRequestException;

    @Operation(summary = "Registrar propietario")
    @PostMapping(value = {"/crear", "/guardar"},
            produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearCliente(@RequestBody Cliente cliente) throws BadRequestException;

    @Operation(summary = "Actualizar propietario")
    @PutMapping(value = "/actualizar",
            produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarCliente(@RequestBody Cliente cliente) throws BadRequestException;
}
