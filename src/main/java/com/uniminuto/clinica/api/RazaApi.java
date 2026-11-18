package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.RazaRq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Razas", description = "Administración gestiona el catálogo y Recepción puede consultarlo.")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/raza")
public interface RazaApi {

    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @Operation(summary = "Registrar raza")
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarRaza(
            @RequestBody RazaRq razaRq)
            throws BadRequestException;

    /** Actualiza una raza existente. */
    @Operation(summary = "Actualizar raza")
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
    @Operation(summary = "Listar razas")
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Raza>> listarRazas()
            throws BadRequestException;

    @Operation(summary = "Buscar razas")
    @GetMapping(value = "/buscar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Raza>> buscarRazas(@RequestParam String termino)
            throws BadRequestException;
}
