package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Interfaz que define los endpoints relacionados con la gestión de fórmulas médicas.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula_medica")
public interface FormulaMedicaApi {

    /**
     * Endpoint para listar todas las fórmulas médicas registradas en el sistema.
     *
     * @return Respuesta HTTP con la lista completa de fórmulas médicas.
     * @throws BadRequestException Si ocurre una validación o un problema de negocio.
     */
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<FormulaMedica>> listarFormulaMedica() throws BadRequestException;

    /**
     * Endpoint dedicado solo a consultar fórmulas médicas por el alias /formulas.
     *
     * @return Respuesta HTTP con la lista de fórmulas médicas.
     * @throws BadRequestException Si ocurre una validación o un problema de negocio.
     */
    @GetMapping(value = "/formulas", produces = {"application/json"})
    ResponseEntity<List<FormulaMedica>> consultarFormulasMedicas() throws BadRequestException;
}