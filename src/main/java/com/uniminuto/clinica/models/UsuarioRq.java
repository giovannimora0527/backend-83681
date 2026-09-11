package com.uniminuto.clinica.models;


import lombok.Data;

@Data
public class UsuarioRq {
    private Long id;

    private String username;

    private String password; // Representa la contraseña antes de encriptarla a passwordHash

    private String rol;

    private String email;

    private Boolean activo;
}
