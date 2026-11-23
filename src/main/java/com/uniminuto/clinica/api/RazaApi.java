package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
<<<<<<< HEAD
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.RazaRq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

=======
import com.uniminuto.clinica.models.RazaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrato REST del catálogo de razas.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
@RequestMapping("/raza")
public interface RazaApi {

    /**
<<<<<<< HEAD
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarRaza(
            @RequestBody RazaRq razaRq)
            throws BadRequestException;

    /** Actualiza una raza existente. */
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarRaza(
            @RequestBody RazaRq razaRq)
            throws BadRequestException;

    /**
     * Metodo que lista las razas del sistema.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Raza>> listarRazas()
=======
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
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
            throws BadRequestException;
}
