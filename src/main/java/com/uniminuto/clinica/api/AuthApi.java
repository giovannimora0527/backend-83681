package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AuthRq;
import com.uniminuto.clinica.models.AuthRs;
import com.uniminuto.clinica.models.CloseSession;
import com.uniminuto.clinica.models.CloseSessionRs;
import com.uniminuto.clinica.models.LoginRs;
import com.uniminuto.clinica.models.TokenRq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Autenticación", description = "Inicio de sesión mediante contraseña y código de correo. Usa el JWT de la validación en el botón Authorize.")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/auth")
public interface AuthApi {

   @Operation(summary = "Solicitar código de inicio de sesión",
           description = "Valida la contraseña, envía el OTP al correo de la cuenta y devuelve usuarioId.")
   @PostMapping(value = "/login",
           produces = {"application/json"},
           consumes = {"application/json"})
   ResponseEntity<AuthRs> login(
           @RequestBody AuthRq authRq)
           throws BadRequestException;

    @Operation(summary = "Validar OTP y obtener JWT",
            description = "Envía el usuarioId del login y el código recibido. El campo token de la respuesta es el JWT.")
    @PostMapping(value = "/validate-token",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<LoginRs> validateToken(
            @RequestBody TokenRq tokenRq)
            throws BadRequestException;


    @Operation(summary = "Solicitar cierre de sesión",
            description = "La ruta requiere una sesión. La invalidación del JWT aún no está implementada.")
    @PostMapping(value = "/cerrar-sesion",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<CloseSessionRs> cerrarSesion(
            @RequestBody CloseSession closeSession)
            throws BadRequestException;
}
