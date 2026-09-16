package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para gestionar la API de citas médicas.
 */
@RestController
@RequestMapping("/api/citas")
public class CitaApiController {

    @Autowired
    private CitaService citaService;

    /**
     * Endpoint para filtrar citas por fecha inicial y final.
     * 
     * @param fechaInicio Fecha de inicio en formato ISO.
     * @param fechaFin Fecha de fin en formato ISO.
     * @return Lista de citas dentro del rango.
     */
    @GetMapping("/filtrar")
    public ResponseEntity<List<Cita>> filtrarCitas(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        List<Cita> citas = citaService.filtrarPorFechas(fechaInicio, fechaFin);
        return ResponseEntity.ok(citas);
    }

    /**
     * Endpoint para guardar/crear una nueva cita.
     * 
     * @param cita Objeto cita enviado en el body.
     * @return Cita guardada con estado 201 Created.
     */
    @PostMapping
    public ResponseEntity<Cita> crearCita(@RequestBody Cita cita) {
        Cita nuevaCita = citaService.guardarCita(cita);
        return new ResponseEntity<>(nuevaCita, HttpStatus.CREATED);
    }

    /**
     * Endpoint para actualizar una cita almacenada.
     * 
     * @param id Identificador de la cita.
     * @param cita Objeto con los nuevos datos.
     * @return Cita actualizada con estado 200 OK.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Cita> actualizarCita(@PathVariable Long id, @RequestBody Cita cita) {
        Cita citaActualizada = citaService.actualizarCita(id, cita);
        return ResponseEntity.ok(citaActualizada);
    }
}