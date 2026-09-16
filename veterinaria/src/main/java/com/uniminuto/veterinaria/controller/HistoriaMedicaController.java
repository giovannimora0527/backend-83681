package com.uniminuto.veterinaria.controller;

import com.uniminuto.veterinaria.entity.HistoriaMedica;
import com.uniminuto.veterinaria.service.HistoriaMedicaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para gestionar la entidad HistoriaMedica.
 */
@RestController
@RequestMapping("/api/historias-medicas")
public class HistoriaMedicaController {

    private final HistoriaMedicaService service;

    public HistoriaMedicaController(HistoriaMedicaService service) {
        this.service = service;
    }

    /**
     * Endpoint para crear una historia médica. (Requerimiento 5)
     * @param historia Datos de la historia médica.
     * @return Historia médica guardada.
     */
    @PostMapping
    public ResponseEntity<HistoriaMedica> crearHistoria(@RequestBody HistoriaMedica historia) {
        return ResponseEntity.ok(service.guardar(historia));
    }

    /**
     * Endpoint para actualizar una historia médica. (Requerimiento 5)
     * @param id Identificador de la historia.
     * @param historia Datos actualizados.
     * @return Historia médica actualizada.
     */
    @PutMapping("/{id}")
    public ResponseEntity<HistoriaMedica> actualizarHistoria(@PathVariable Long id, @RequestBody HistoriaMedica historia) {
        historia.setId(id);
        return ResponseEntity.ok(service.guardar(historia));
    }

    /**
     * Endpoint para listar historias médicas filtradas por fecha. (Requerimiento 5)
     * @param inicio Fecha inicial.
     * @param fin Fecha final.
     * @return Lista de historias médicas ordenadas por fecha reciente.
     */
    @GetMapping("/filtrar")
    public ResponseEntity<List<HistoriaMedica>> filtrarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(service.filtrarPorFecha(inicio, fin));
    }
}