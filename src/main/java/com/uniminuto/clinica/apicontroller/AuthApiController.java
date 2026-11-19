package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.AuthApi;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AuthRq;
import com.uniminuto.clinica.models.AuthRs;
import com.uniminuto.clinica.models.CloseSession;
import com.uniminuto.clinica.models.CloseSessionRs;
import com.uniminuto.clinica.models.LoginRs;
import com.uniminuto.clinica.models.TokenRq;
import com.uniminuto.clinica.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Expone las operaciones HTTP relacionadas con la autenticación y sesión. */
@RestController
public class AuthApiController implements AuthApi {

    @Autowired
    private AuthService authService;

    /** Comprueba las credenciales e inicia la verificación mediante un código temporal. */
    @Override
    public ResponseEntity<AuthRs> login(AuthRq authRq) throws BadRequestException {
        // Implement the login logic here
        return ResponseEntity.ok(this.authService.login(authRq));
    }

    /** Recibe el código de verificación y solicita completar el inicio de sesión. */
    @Override
    public ResponseEntity<LoginRs> validateToken(TokenRq tokenRq) throws BadRequestException {
        return ResponseEntity.ok(this.authService.validateToken(tokenRq));
    }

    /** Elimina la sesión asociada al token recibido. */
    @Override
    public ResponseEntity<CloseSessionRs> cerrarSesion(CloseSession closeSession) throws BadRequestException {
        return ResponseEntity.ok(this.authService.cerrarSesion(closeSession));
    }
}
