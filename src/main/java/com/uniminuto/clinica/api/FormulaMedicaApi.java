package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Contrato REST de los servicios expuestos para la entidad FormulaMedica.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {

    /**
     * Lista las formulas medicas del inventario de la mas reciente a la mas
     * antigua segun su fecha de creacion.
     *
     * @return Lista de formulas medicas.
     * @throws BadRequestException Excepcion de negocio.
     */
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<FormulaMedica>> listarFormulasMedicas()
            throws BadRequestException;
}
