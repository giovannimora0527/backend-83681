package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class UsuarioRq {

    private Long id;

    private String username;

    private String email;

    private String passwordHash;

    private String rol;

    private Boolean activo;
}
