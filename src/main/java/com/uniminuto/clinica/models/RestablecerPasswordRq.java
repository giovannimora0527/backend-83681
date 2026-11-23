package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Solicitud para restablecer la contraseña con el código enviado al correo.
 */
@Data
public class RestablecerPasswordRq {
    /**
     * Correo electrónico registrado del usuario.
     */
    private String email;
    /**
     * Código de verificación recibido por correo.
     */
    private String token;
    /**
     * Nueva contraseña en texto plano (se almacena con BCrypt).
     */
    private String nuevaPassword;
}
