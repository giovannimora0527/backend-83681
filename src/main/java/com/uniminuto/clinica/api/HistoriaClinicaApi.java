package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaClinicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrato (endpoints) del modulo de historias clinicas.
 * Ruta base: /clinica/v1/historia-clinica
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/historia-clinica")
public interface HistoriaClinicaApi {

    /**
     * Lista todas las historias clinicas, de la consulta mas reciente a la mas antigua.
     *
     * @return lista de historias.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<HistoriaMedica>> listarHistorias() throws BadRequestException;

    /**
     * Consulta una historia clinica por id.
     *
     * @param historiaId id de la historia.
     * @return historia encontrada (404 si no existe).
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/consultar", produces = {"application/json"})
    ResponseEntity<HistoriaMedica> consultarHistoria(@RequestParam Long historiaId) throws BadRequestException;

    /**
     * Crea una historia clinica a partir de una cita atendida. Responde 201.
     *
     * @param historiaRq datos de la historia.
     * @return mensaje de exito.
     * @throws BadRequestException si alguna regla no se cumple.
     */
    @PostMapping(value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearHistoria(@RequestBody HistoriaClinicaRq historiaRq)
            throws BadRequestException;

    /**
     * Cierra una historia clinica: ya no acepta nuevas anotaciones.
     *
     * @param historiaId id de la historia.
     * @return mensaje de exito.
     * @throws BadRequestException si ya estaba cerrada.
     */
    @PatchMapping(value = "/cerrar", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> cerrarHistoria(@RequestParam Long historiaId) throws BadRequestException;
}
