package com.uniminuto.veterinaria.controller;

import com.uniminuto.veterinaria.entity.FormulaMedica;
import com.uniminuto.veterinaria.service.FormulaMedicaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * Controlador REST para gestionar la entidad FormulaMedica.
 */
@RestController
@RequestMapping("/api/formulas")
public class FormulaMedicaController {

    private final FormulaMedicaService service;

    public FormulaMedicaController(FormulaMedicaService service) {
        this.service = service;
    }

    /**
     * Endpoint para obtener todas las fórmulas médicas ordenadas por fecha reciente.
     * @return Respuesta HTTP con la lista de fórmulas médicas.
     */
    @GetMapping
    public ResponseEntity<List<FormulaMedica>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }
}