package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
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
 * Contrato REST de los servicios expuestos para la entidad HistoriaMedica.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/historia-medica")
public interface HistoriaMedicaApi {

    /**
     * Lista las historias medicas filtradas por un rango de fechas opcional,
     * ordenadas de la mas reciente a la mas antigua.
     *
     * @param fechaInicial Fecha inicial del rango con formato yyyy-MM-dd.
     * @param fechaFinal Fecha final del rango con formato yyyy-MM-dd.
     * @return Lista de historias medicas encontradas.
     * @throws BadRequestException Excepcion de negocio.
     */
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<HistoriaMedica>> listarHistoriasMedicas(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicial,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinal)
            throws BadRequestException;

    /**
     * Crea una nueva historia medica.
     *
     * @param historiaMedicaRq Datos de la historia medica a crear.
     * @return Respuesta con el estado de la operacion.
     * @throws BadRequestException Excepcion de negocio.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarHistoriaMedica(
            @RequestBody HistoriaMedicaRq historiaMedicaRq)
            throws BadRequestException;

    /**
     * Actualiza una historia medica existente.
     *
     * @param historiaMedicaRq Datos de la historia medica a actualizar.
     * @return Respuesta con el estado de la operacion.
     * @throws BadRequestException Excepcion de negocio.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarHistoriaMedica(
            @RequestBody HistoriaMedicaRq historiaMedicaRq)
            throws BadRequestException;
}
