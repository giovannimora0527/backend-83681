package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.uniminuto.clinica.exception.BadRequestException;

/** Agrupa las operaciones de comprobación de la aplicación. */
@Tag(name = "Diagnóstico del backend", description = "Administración. Rutas de comprobación de funcionamiento.")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/clinica")
public interface ClinicaApi {

    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @Operation(summary = "Comprobar servicio de clínica")
    @GetMapping(value = "/test",
            produces = {"application/text"},
            consumes = {"application/json"})
    ResponseEntity<String> testService()
            throws BadRequestException;


    /** Devuelve la respuesta de la segunda comprobación del servicio. */
    @Operation(summary = "Comprobar respuesta de texto de la clínica")
    @GetMapping(value = "/test2",
            produces = {"application/text"},
            consumes = {"application/json"})
    ResponseEntity<String> testService2()
            throws BadRequestException;

    /** Devuelve la respuesta de la tercera comprobación del servicio. */
    @Operation(summary = "Comprobar respuesta JSON de la clínica")
    @GetMapping(value = "/test3",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> testService3()
            throws BadRequestException;
}
