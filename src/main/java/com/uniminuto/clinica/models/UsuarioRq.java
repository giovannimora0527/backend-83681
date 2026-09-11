package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class UsuarioRq {

    private Long usuarioId;

    private String nombre;

    private String email;

    private String password;

    private String rol;
}
