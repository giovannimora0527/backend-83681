package com.uniminuto.clinica.service;

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
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthService {

    AuthRs login(AuthRq authRq) throws BadRequestException;

    LoginRs validateToken(TokenRq tokenRq) throws BadRequestException;

    CloseSessionRs cerrarSesion(CloseSession closeSession)
            throws BadRequestException;

    /**
     * Genera y envía un código de recuperación al correo del usuario.
     * Responde siempre lo mismo para no revelar qué correos están registrados.
     */
    MiRespuestaRS recuperarPassword(RecuperarPasswordRq request) throws BadRequestException;

    /**
     * Valida el código de recuperación y guarda la nueva contraseña.
     */
    MiRespuestaRS restablecerPassword(RestablecerPasswordRq request) throws BadRequestException;
}
