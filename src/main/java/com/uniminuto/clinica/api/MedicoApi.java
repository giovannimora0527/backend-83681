package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrato REST del recurso médico (veterinario).
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/medico")
public interface MedicoApi {

    /**
     * Lista los médicos ordenados por apellidos y nombres.
     *
     * @return lista de médicos.
     * @throws BadRequestException si ocurre un error de negocio.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Medico>> listarMedicos()
            throws BadRequestException;

    /**
     * Busca un médico por tipo y número de documento.
     *
     * @param tipoDocumento   tipo de documento.
     * @param numeroDocumento número de documento.
     * @return médico encontrado.
     * @throws BadRequestException si no existe.
     */
    @GetMapping(value = "/buscar",
            produces = {"application/json"})
    ResponseEntity<Medico> buscarMedico(
            @RequestParam String tipoDocumento,
            @RequestParam String numeroDocumento)
            throws BadRequestException;

    /**
     * Busca un médico por su registro profesional.
     *
     * @param registroProfesional registro profesional.
     * @return médico encontrado.
     * @throws BadRequestException si no existe.
     */
    @GetMapping(value = "/buscar-registro",
            produces = {"application/json"})
    ResponseEntity<Medico> buscarMedicoPorRegistroProfesional(
            @RequestParam String registroProfesional)
            throws BadRequestException;

    /**
     * Registra un nuevo médico.
     *
     * @param medicoRq datos del médico.
     * @return resultado de la operación.
     * @throws BadRequestException si los datos no son válidos.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarMedico(
            @RequestBody MedicoRq medicoRq)
            throws BadRequestException;

    /**
     * Actualiza un médico existente.
     *
     * @param medicoRq datos actualizados.
     * @return resultado de la operación.
     * @throws BadRequestException si no existe o los datos no son válidos.
     */
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarMedico(
            @RequestBody MedicoRq medicoRq)
            throws BadRequestException;
}
