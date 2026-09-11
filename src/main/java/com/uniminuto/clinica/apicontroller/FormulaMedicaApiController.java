package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/formulamedica")
public class FormulaMedicaApiController implements FormulaMedicaApi {

    @Autowired
    private FormulaMedicaService formulaMedicaService;

    @Override
    @GetMapping("/listar")
    public ResponseEntity<List<FormulaMedica>> listarFormulasMedicas()
            throws BadRequestException {
        List<FormulaMedica> formulaMedicas = formulaMedicaService.obtenerFormulasOrdenadas();
        return ResponseEntity.ok(formulaMedicas);
    }
}
