package com.uniminuto.clinica.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioRequestDTO {
    private String username;
    private String password; // texto plano, se cifra en el service
    private String rol;
    private String email;
    private Boolean activo;
}