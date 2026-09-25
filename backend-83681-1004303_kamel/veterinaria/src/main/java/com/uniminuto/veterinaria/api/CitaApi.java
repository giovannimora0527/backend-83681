package com.uniminuto.veterinaria.api;

import com.uniminuto.veterinaria.entity.Cita;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Interfaz API para la gestión del recurso Citas Médicas.
 */
@RequestMapping("/api/citas")
public interface CitaApi {

    @GetMapping
    ResponseEntity<List<Cita>> listarTodas();

    @GetMapping("/filtrar")
    ResponseEntity<List<Cita>> filtrarPorFecha(
            @RequestParam("inicio") String inicio,
            @RequestParam("fin") String fin);

    @PostMapping
    ResponseEntity<Cita> crearCita(@RequestBody Cita cita);

    @PutMapping("/{id}")
    ResponseEntity<Cita> actualizarCita(@PathVariable("id") Long id, @RequestBody Cita cita);
}