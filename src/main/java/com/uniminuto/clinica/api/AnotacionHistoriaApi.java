package com.uniminuto.clinica.api;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.AnotacionHistoriaRs;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/anotacion")
public interface AnotacionHistoriaApi {

    /**
     * Lista las anotaciones filtradas por rango de fecha, ordenadas descendentemente.
     *
     * @param fechaInicio Fecha inicial (YYYY-MM-DD).
     * @param fechaFin Fecha final (YYYY-MM-DD).
     * @return Lista de anotaciones.
     * @throws BadRequestException Excepción de validación.
     */
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<AnotacionHistoriaRs>> listarAnotaciones(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin)
            throws BadRequestException;

    /**
     * Crea una nueva anotación en la historia médica.
     *
     * @param anotacionRq Datos de la anotación.
     * @return Anotación creada.
     * @throws BadRequestException Excepción de validación.
     */
    @PostMapping(value = "/crear", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<AnotacionHistoriaRs> crearAnotacion(@RequestBody AnotacionHistoriaRq anotacionRq) throws BadRequestException;

    /**
     * Actualiza una anotación existente.
     *
     * @param anotacionRq Datos actualizados.
     * @return Anotación actualizada.
     * @throws BadRequestException Excepción de validación.
     */
    @PostMapping(value = "/actualizar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<AnotacionHistoriaRs> actualizarAnotacion(@RequestBody AnotacionHistoriaRq anotacionRq) throws BadRequestException;
}
