package com.uniminuto.clinica.models;

import lombok.Data;

/** Contiene la respuesta de la primera etapa del inicio de sesión. */
@Data
public class AuthRs {
    private String username;
    private Long usuarioId;
    private Boolean login;
}
