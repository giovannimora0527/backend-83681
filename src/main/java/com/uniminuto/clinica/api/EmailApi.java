package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.uniminuto.clinica.models.TestRs;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/** Agrupa las operaciones HTTP relacionadas con el envío de correos. */
@Tag(name = "Correo", description = "Administración. Comprobación del servicio de correo.")
@CrossOrigin(origins = "*")
@RequestMapping("/email")
public interface EmailApi {

    /**
     * Metodo test del servicio de envio de email.
     *
     * @return Servicio ok.
     * @throws BadRequestException excepcion.
     */
    @Operation(summary = "Comprobar servicio de correo")
    @RequestMapping(value = "/test",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.GET)
    ResponseEntity<TestRs> testEmail()
            throws BadRequestException;
}
