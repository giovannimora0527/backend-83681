package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.uniminuto.clinica.models.UsuarioRS;

import java.util.List;


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula-medica")
/**
 * Interfaz que define los endpoints relacionados con la gestión de fórmulas médicas.
 */
public interface FormulaMedicaApi {
    /**
     * Endpoint para listar todas las fórmulas médicas registradas en el sistema.
     *
     * @return Respuesta HTTP con la lista completa de fórmulas médicas.
     * @throws BadRequestException Si ocurre una validación o un problema de negocio.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<FormulaMedica>> listarFormulasMedicas()
            throws BadRequestException;

    /**
     * Endpoint dedicado solo a consultar fórmulas médicas por el alias /formulas.
     *
     * @return Respuesta HTTP con la lista de fórmulas médicas.
     * @throws BadRequestException Si ocurre una validación o un problema de negocio.
     */
    @GetMapping(value = "/formulas",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<FormulaMedica>> consultarFormulasMedicas()
            throws BadRequestException;

    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<UsuarioRS> guardarFormulaMedica(@RequestBody FormulaMedica formula)
            throws BadRequestException;

    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<UsuarioRS> actualizarFormulaMedica(@RequestBody FormulaMedica formula)
            throws BadRequestException;
}
