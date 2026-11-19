package com.uniminuto.clinica.service;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AuthRq;
import com.uniminuto.clinica.models.AuthRs;
import com.uniminuto.clinica.models.CloseSession;
import com.uniminuto.clinica.models.CloseSessionRs;
import com.uniminuto.clinica.models.LoginRs;
import com.uniminuto.clinica.models.TokenRq;
import org.springframework.web.bind.annotation.RequestBody;

/** Define las operaciones disponibles para gestionar la autenticación y sesión. */
public interface AuthService {

    /** Comprueba las credenciales e inicia la verificación mediante un código temporal. */
    AuthRs login(AuthRq authRq) throws BadRequestException;

    /** Recibe el código de verificación y solicita completar el inicio de sesión. */
    LoginRs validateToken(TokenRq tokenRq) throws BadRequestException;

    /** Elimina la sesión asociada al token recibido. */
    CloseSessionRs cerrarSesion(CloseSession closeSession)
            throws BadRequestException;
}
