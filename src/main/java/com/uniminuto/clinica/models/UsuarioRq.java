package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class UsuarioRq {
    /**
     * Representacion de usuario: No incluye el password.
     */
    private Long id;

    private String username;

    private String email;

    private String passwordHash;

    private String rol;

    private Boolean activo;
}
