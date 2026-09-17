package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * Controlador REST para la gestion de formulas medicas.
 */
// apicontroller/FormulaMedicaApiController.java
@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {
    @Autowired
    private FormulaMedicaService formulaMedicaService;

    @Override
    public ResponseEntity<List<FormulaMedica>> listar() throws BadRequestException {
        return ResponseEntity.ok(formulaMedicaService.listar());
    }
}
