package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class UsuarioRq {
    private Long id;
    private String userName;
    private String password;
    private String tipoDocumento;
    private String rol;
    private Boolean activo;
    private String email;
}