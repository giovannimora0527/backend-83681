package com.uniminuto.veterinaria.controller;

import com.uniminuto.veterinaria.entity.Cita;
import com.uniminuto.veterinaria.service.CitaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para gestionar la entidad Cita.
 */
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService service;

    public CitaController(CitaService service) {
        this.service = service;
    }

    /**
     * Endpoint para listar todas las citas del sistema. (Requerimiento 2)
     * @return Lista general de citas.
     */
    @GetMapping
    public ResponseEntity<List<Cita>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    /**
     * Endpoint para filtrar citas por rango de fechas ordenadas por la más reciente. (Requerimiento 3)
     * @param inicio Fecha inicial (YYYY-MM-DD).
     * @param fin Fecha final (YYYY-MM-DD).
     * @return Lista de citas filtradas.
     */
    @GetMapping("/filtrar")
    public ResponseEntity<List<Cita>> filtrarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(service.filtrarPorFecha(inicio, fin));
    }

    /**
     * Endpoint para crear una nueva cita. (Requerimiento 4)
     * @param cita Datos de la nueva cita.
     * @return Cita creada.
     */
    @PostMapping
    public ResponseEntity<Cita> crearCita(@RequestBody Cita cita) {
        return ResponseEntity.ok(service.guardar(cita));
    }

    /**
     * Endpoint para actualizar una cita existente. (Requerimiento 4)
     * @param id Identificador de la cita a actualizar.
     * @param cita Detalle actualizado.
     * @return Cita actualizada.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Cita> actualizarCita(@PathVariable Long id, @RequestBody Cita cita) {
        cita.setId(id);
        return ResponseEntity.ok(service.guardar(cita));
    }
}