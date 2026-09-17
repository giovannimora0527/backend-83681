package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato REST de los servicios expuestos para la entidad Cita.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cita")
public interface CitaApi {

    /**
     * Filtra las citas entre una fecha inicial y una fecha final, ordenadas de
     * la mas reciente a la mas antigua.
     *
     * @param fechaInicial Fecha inicial del rango con formato yyyy-MM-dd.
     * @param fechaFinal Fecha final del rango con formato yyyy-MM-dd.
     * @return Lista de citas encontradas.
     * @throws BadRequestException Excepcion de negocio.
     */
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<Cita>> listarCitasPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinal)
            throws BadRequestException;

    /**
     * Crea una nueva cita en el sistema.
     *
     * @param citaRq Datos de la cita a crear.
     * @return Respuesta con el estado de la operacion.
     * @throws BadRequestException Excepcion de negocio.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarCita(@RequestBody CitaRq citaRq)
            throws BadRequestException;

    /**
     * Actualiza una cita existente en el sistema.
     *
     * @param citaRq Datos de la cita a actualizar.
     * @return Respuesta con el estado de la operacion.
     * @throws BadRequestException Excepcion de negocio.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarCita(@RequestBody CitaRq citaRq)
            throws BadRequestException;
}
