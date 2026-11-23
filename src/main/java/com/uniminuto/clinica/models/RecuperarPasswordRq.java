package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Solicitud para iniciar la recuperación de contraseña.
 */
@Data
public class RecuperarPasswordRq {
    /**
     * Correo electrónico registrado del usuario.
     */
    private String email;
}
