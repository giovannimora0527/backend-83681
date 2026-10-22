package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.RazaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrato REST del catálogo de razas.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/raza")
public interface RazaApi {

    /**
     * Lista las razas ordenadas por nombre.
     *
     * @return lista de razas.
     * @throws BadRequestException si ocurre un error de negocio.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Raza>> listarRazas()
            throws BadRequestException;

    /**
     * Busca una raza por nombre.
     *
     * @param nombre nombre de la raza.
     * @return raza encontrada.
     * @throws BadRequestException si no existe.
     */
    @GetMapping(value = "/buscar",
            produces = {"application/json"})
    ResponseEntity<Raza> buscarRazaPorNombre(
            @RequestParam String nombre)
            throws BadRequestException;

    /**
     * Registra una nueva raza.
     *
     * @param razaRq datos de la raza.
     * @return resultado de la operación.
     * @throws BadRequestException si los datos no son válidos.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarRaza(
            @RequestBody RazaRq razaRq)
            throws BadRequestException;

    /**
     * Actualiza una raza existente.
     *
     * @param razaRq datos actualizados de la raza.
     * @return resultado de la operación.
     * @throws BadRequestException si la raza no existe o los datos no son válidos.
     */
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarRaza(
            @RequestBody RazaRq razaRq)
            throws BadRequestException;
}
