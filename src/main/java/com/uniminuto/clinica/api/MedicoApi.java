package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
<<<<<<< HEAD
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
=======
import org.springframework.web.bind.annotation.*;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661

import java.time.LocalDateTime;
import java.util.List;

<<<<<<< HEAD
@RequestMapping("/medico")
public interface MedicoApi {

    @GetMapping(value = "/listar-disponibles",
            produces = {"application/json"})
    ResponseEntity<List<Medico>> listarMedicosDisponibles(
            @RequestParam LocalDateTime fechaInicio,
            @RequestParam LocalDateTime fechaFin)
            throws BadRequestException;


    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Medico>> listarTodos()
            throws BadRequestException;

    @PostMapping(value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearMedico(
            @RequestBody MedicoRq medicoRq
    )
            throws BadRequestException;

    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarMedico(
            @RequestBody MedicoRq medicoRq
    )
=======
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
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
            throws BadRequestException;
}
