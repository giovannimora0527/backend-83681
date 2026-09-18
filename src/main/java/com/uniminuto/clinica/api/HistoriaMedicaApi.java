package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/historia-medica")
public interface HistoriaMedicaApi {

    /**
     * Lista todas las historias médicas, de la más reciente a la más antigua.
     *
     * @return lista de historias médicas.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<HistoriaMedica>> getHistoriasMedicas()
            throws BadRequestException;

    /**
     * Registra una nueva historia médica para un paciente.
     *
     * @param historiaMedicaRq datos de la historia médica a crear.
     * @return mensaje de confirmación.
     * @throws BadRequestException excepcion.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarHistoriaMedica(
            @RequestBody HistoriaMedicaRq historiaMedicaRq)
            throws BadRequestException;

    /**
     * Actualiza una historia médica ya almacenada en el sistema.
     *
     * @param historiaMedicaRq datos nuevos de la historia médica, incluyendo su id.
     * @return mensaje de confirmación.
     * @throws BadRequestException excepcion.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarHistoriaMedica(
            @RequestBody HistoriaMedicaRq historiaMedicaRq)
            throws BadRequestException;
}