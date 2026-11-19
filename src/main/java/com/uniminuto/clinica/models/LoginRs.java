package com.uniminuto.clinica.models;

import lombok.Data;

/** Contiene el resultado de la autenticación y el token de sesión. */
@Data
public class LoginRs {
    private String token;
    private Boolean logueoExitoso;
}
