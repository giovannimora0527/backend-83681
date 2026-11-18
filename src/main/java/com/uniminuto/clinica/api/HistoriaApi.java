package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.HistoriaMedicaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/** Expone el historial clínico y permite registrar o corregir el diagnóstico de una cita abierta. */
@Tag(name = "Historia clínica", description = "Administración y Veterinaria. El servicio comprueba el acceso a las historias y citas.")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/historia-clinica")
public interface HistoriaApi {

    @Operation(summary = "Listar historias clínicas")
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<HistoriaMedicaRs>> listarAllHIstorias()
            throws BadRequestException;

    @Operation(summary = "Buscar historias clínicas")
    @GetMapping(value = "/buscar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<HistoriaMedicaRs>> buscarHistorias(@RequestParam String termino)
            throws BadRequestException;

    @Operation(summary = "Registrar historia de una cita")
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarHistoria(
            @RequestBody HistoriaMedicaRq historiaMedicaRq
    )
            throws BadRequestException;

    @Operation(summary = "Actualizar historia de una cita abierta")
    @RequestMapping(value = "/actualizar", method = {RequestMethod.POST, RequestMethod.PUT},
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarHistoria(
            @RequestBody HistoriaMedicaRq historiaMedicaRq
    )
            throws BadRequestException;
}
