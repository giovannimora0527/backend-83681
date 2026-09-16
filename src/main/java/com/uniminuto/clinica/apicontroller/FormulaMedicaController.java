package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para exponer los servicios de fórmulas médicas.
 */
@RestController
@RequestMapping("/api/formulas-medicas")
public class FormulaMedicaController {

    @Autowired
    private FormulaMedicaService formulaMedicaService;

    /**
     * Endpoint para listar las fórmulas médicas ordenadas por fecha de la más reciente a la más antigua.
     * 
     * @return Respuesta HTTP con la lista de fórmulas médicas.
     */
    @GetMapping
    public ResponseEntity<List<FormulaMedica>> listarFormulasMedicas() {
        List<FormulaMedica> formulas = formulaMedicaService.obtenerFormulasOrdenadas();
        return ResponseEntity.ok(formulas);
    }

    /**
     * Endpoint para crear una nueva fórmula médica.
     * 
     * @param formulaMedica Datos de la fórmula médica recibidos en el cuerpo de la petición.
     * @return Respuesta HTTP 201 Created con la fórmula médica guardada.
     */
    @PostMapping
    public ResponseEntity<FormulaMedica> crearFormulaMedica(@RequestBody FormulaMedica formulaMedica) {
        FormulaMedica guardada = formulaMedicaService.guardarFormula(formulaMedica);
        return new ResponseEntity<>(guardada, HttpStatus.CREATED);
    }
}