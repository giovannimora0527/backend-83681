package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {

    @GetMapping(
            value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<List<FormulaMedica>> listarFormulas()
            throws BadRequestException;

    @GetMapping(value = "/{id}", produces = {"application/json"})
    ResponseEntity<FormulaMedica> buscarPorId(@PathVariable Long id)
            throws BadRequestException;

    @PostMapping(
            value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<MiRespuestaRS> crear(
            @RequestBody FormulaMedicaRq solicitud)
            throws BadRequestException;

    @PutMapping(
            value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<MiRespuestaRS> actualizar(
            @RequestBody FormulaMedicaRq solicitud)
            throws BadRequestException;
}