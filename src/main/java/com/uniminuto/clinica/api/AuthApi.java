package com.uniminuto.clinica.api;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AuthRq;
import com.uniminuto.clinica.models.AuthRs;
import com.uniminuto.clinica.models.CloseSession;
import com.uniminuto.clinica.models.CloseSessionRs;
import com.uniminuto.clinica.models.LoginRs;
import com.uniminuto.clinica.models.TokenRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.RecuperarPasswordRq;
import com.uniminuto.clinica.models.RestablecerPasswordRq;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/auth")
public interface AuthApi {

   @PostMapping(value = "/login",
           produces = {"application/json"},
           consumes = {"application/json"})
   ResponseEntity<AuthRs> login(
           @RequestBody AuthRq authRq)
           throws BadRequestException;

    @PostMapping(value = "/validate-token",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<LoginRs> validateToken(
            @RequestBody TokenRq tokenRq)
            throws BadRequestException;


    @PostMapping(value = "/recover-password",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> recuperarPassword(
            @RequestBody RecuperarPasswordRq request)
            throws BadRequestException;

    @PostMapping(value = "/reset-password",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> restablecerPassword(
            @RequestBody RestablecerPasswordRq request)
            throws BadRequestException;

    @PostMapping(value = "/cerrar-sesion",
            produces = {"application/json"})
    ResponseEntity<CloseSessionRs> cerrarSesion(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization)
            throws BadRequestException;
}
