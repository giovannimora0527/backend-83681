package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Contrato REST para la gestión de fórmulas médicas del inventario.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {

    /**
     * Lista las fórmulas médicas del inventario ordenadas por fecha de
     * creación, de la más reciente a la más antigua.
     *
     * @return lista de fórmulas médicas.
     * @throws BadRequestException excepción en caso de error de negocio.
     */
    @GetMapping(value = "/listar-ordenado",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<FormulaMedica>> listarFormulasMedicas()
            throws BadRequestException;
}
