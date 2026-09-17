package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para Fórmulas Médicas.
 */
@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {

    @Autowired
    private FormulaMedicaService formulaMedicaService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<FormulaMedica>> listarFormulasMedicas() {
        // 1. Llamamos al servicio para obtener los datos
        List<FormulaMedica> formulas = formulaMedicaService.obtenerFormulasOrdenadas();

        // 2. Retornamos la respuesta
        return ResponseEntity.ok(formulas);
    }
}