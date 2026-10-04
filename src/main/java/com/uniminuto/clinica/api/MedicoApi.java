package com.uniminuto.clinica.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/medico")
public interface MedicoApi {

    @GetMapping(value = "/all", produces = {"application/json"})
    ResponseEntity<List<Medico>> getMedicos() throws BadRequestException;

    @GetMapping(value = "/buscar", produces = {"application/json"})
    ResponseEntity<Medico> buscarMedico(
            @RequestParam String tipoDocumento,
            @RequestParam String numeroDocumento) throws BadRequestException;

    @GetMapping(value = "/buscarPorRegistroProfesional", produces = {"application/json"})
    ResponseEntity<Medico> buscarMedicoPorRegistroProfesional(
            @RequestParam String registroProfesional) throws BadRequestException;

    @PostMapping(value = "/guardar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarMedico(@RequestBody MedicoRq medicoRq) throws BadRequestException;

    @PostMapping(value = "/actualizar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarMedico(@RequestBody MedicoRq medicoRq) throws BadRequestException;
}