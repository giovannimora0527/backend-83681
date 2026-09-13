package com.uniminuto.clinica.api;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.CitaRs;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cita")

public interface CitaApi {

    /**
     * Método para filtrar citas por rango de fecha.
     *
     * @param fechaInicio Fecha inicial en formato YYYY-MM-DD
     * @param fechaFin Fecha final en formato YYYY-MM-DD
     * @return Lista de citas que coinciden con el filtro.
     * @throws BadRequestException en caso de parámetros inválidos.
     */

    @GetMapping(value = "/filtrar", produces = {"application/json"})
    ResponseEntity<List<CitaRs>> filtrarCitas(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin)
            throws BadRequestException;

    /**
     * Crea una nueva cita en el sistema.
     *
     * @param citaRq Datos de la cita.
     * @return Cita creada.
     * @throws BadRequestException Excepción de validación.
     */
    @PostMapping(value = "/crear", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<CitaRs> crearCita(@RequestBody CitaRq citaRq) throws BadRequestException;

    /**
     * Actualiza los datos de una cita existente.
     *
     * @param citaRq Datos a actualizar.
     * @return Cita actualizada.
     * @throws BadRequestException Excepción de validación.
     */
    @PostMapping(value = "/actualizar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<CitaRs> actualizarCita(@RequestBody CitaRq citaRq) throws BadRequestException;
}
