package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.EspecializacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrato REST del catálogo de especializaciones médicas.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/especializacion")
public interface EspecializacionApi {

    /**
     * Lista las especializaciones ordenadas por nombre.
     *
     * @return lista de especializaciones.
     * @throws BadRequestException si ocurre un error de negocio.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Especializacion>> listarEspecializaciones()
            throws BadRequestException;

    /**
     * Busca una especialización por su código.
     *
     * @param codigo código de la especialización.
     * @return especialización encontrada.
     * @throws BadRequestException si no existe.
     */
    @GetMapping(value = "/buscar",
            produces = {"application/json"})
    ResponseEntity<Especializacion> buscarEspecializacionPorCodigo(
            @RequestParam String codigo)
            throws BadRequestException;

    /**
     * Registra una especialización.
     *
     * @param especializacionRq datos de la especialización.
     * @return resultado de la operación.
     * @throws BadRequestException si los datos no son válidos.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarEspecializacion(
            @RequestBody EspecializacionRq especializacionRq)
            throws BadRequestException;

    /**
     * Actualiza una especialización existente.
     *
     * @param especializacionRq datos actualizados.
     * @return resultado de la operación.
     * @throws BadRequestException si no existe o los datos no son válidos.
     */
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarEspecializacion(
            @RequestBody EspecializacionRq especializacionRq)
            throws BadRequestException;
}
