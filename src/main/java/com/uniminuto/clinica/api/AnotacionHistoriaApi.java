package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/anotacion-historia")
public interface AnotacionHistoriaApi {

    /**
     * Metodo para listar las anotaciones de una historia clinica específica.
     *
     * @param historiaId Identificador de la historia medica.
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/listar/{historiaId}",
            produces = {"application/json"})
    ResponseEntity<List<AnotacionHistoria>> listarAnotacionesPorHistoria(@PathVariable("historiaId") Long historiaId)
            throws BadRequestException;

    /**
     * Metodo para crear una nueva anotacion en la historia clinica.
     *
     * @param request Objeto con los datos de la anotacion a crear.
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @PostMapping(value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearAnotacionHistoria(@RequestBody AnotacionHistoriaRq request)
            throws BadRequestException;
}