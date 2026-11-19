package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.AnotacionHistoriaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** Expone las notas clínicas que pueden consultarse como parte de los antecedentes del paciente. */
@Tag(name = "Anotaciones clínicas", description = "Administración y Veterinaria. Notas asociadas a la historia clínica.")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/anotacion-historia")
public interface AnotacionHistoriaApi {

    /** Consulta las anotaciones clínicas registradas. */
    @Operation(summary = "Listar anotaciones")
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<AnotacionHistoriaRs>> listarAnotaciones();

    /** Consulta las anotaciones de la historia indicada. */
    @Operation(summary = "Buscar anotaciones")
    @GetMapping(value = "/buscar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<AnotacionHistoriaRs>> buscarPorHistoria(@RequestParam Long historiaId);

    /**
     * Metodo crear historia medica con anotaciones.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @Operation(summary = "Registrar anotación clínica")
    @PostMapping(value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearAnotacionHistoria(
            @RequestBody AnotacionHistoriaRq anotacionHistoria
    )
            throws BadRequestException;

    /** Guarda los cambios de una anotación clínica existente. */
    @Operation(summary = "Actualizar anotación clínica")
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarAnotacionHistoria(
            @RequestBody AnotacionHistoriaRq anotacionHistoria
    ) throws BadRequestException;
}
