package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador de API REST para la gestión de Anotaciones de Historia Clínica.
 */
@RestController
@RequestMapping("/api/anotaciones-historia")
public class AnotacionHistoriaApiController {

    private final AnotacionHistoriaService anotacionService;

    @Autowired
    public AnotacionHistoriaApiController(AnotacionHistoriaService anotacionService) {
        this.anotacionService = anotacionService;
    }

    /**
     * Endpoint para crear una nueva anotación.
     */
    @PostMapping
    public ResponseEntity<AnotacionHistoria> crearAnotacion(@RequestBody AnotacionHistoria anotacion) {
        AnotacionHistoria creada = anotacionService.crearAnotacion(anotacion);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    /**
     * Endpoint para actualizar una anotación existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AnotacionHistoria> actualizarAnotacion(
            @PathVariable Long id,
            @RequestBody AnotacionHistoria anotacion) {
        AnotacionHistoria actualizada = anotacionService.actualizarAnotacion(id, anotacion);
        return ResponseEntity.ok(actualizada);
    }

    /**
     * Endpoint para listar anotaciones filtradas por rango de fecha desc.
     */
    @GetMapping("/filtrar")
    public ResponseEntity<List<AnotacionHistoria>> listarPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        List<AnotacionHistoria> resultado = anotacionService.listarPorFechas(fechaInicio, fechaFin);
        return ResponseEntity.ok(resultado);
    }
}